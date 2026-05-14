<template>
  <div class="task-list">
    <el-card>
      <div slot="header" class="card-header">
        <span>{{ currentProjectName ? currentProjectName + ' - ' : '' }}任务列表</span>
        <div>
          <el-button size="small" @click="checkDelayedTasks">检查延期任务</el-button>
          <el-button type="primary" size="small" @click="showCreateDialog">新建任务</el-button>
        </div>
      </div>
      <el-table :data="tasks" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="任务名称" width="180"></el-table-column>
        <el-table-column prop="description" label="任务描述" show-overflow-tooltip width="150"></el-table-column>
        <el-table-column prop="assignee" label="负责人" width="100"></el-table-column>
        <el-table-column prop="estimatedDays" label="预计工期(天)" width="100"></el-table-column>
        <el-table-column prop="plannedStartDate" label="计划开始" width="110"></el-table-column>
        <el-table-column prop="plannedEndDate" label="计划结束" width="110"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="前置依赖" width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            <div v-if="scope.row.dependencyIds && scope.row.dependencyIds.length > 0">
              <el-tag size="mini" v-for="depId in scope.row.dependencyIds" :key="depId" style="margin-right: 5px;">
                {{ getTaskName(depId) }}
              </el-tag>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="阻塞信息" width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            <div v-if="scope.row.status === 'BLOCKED'">
              <div><strong>原因:</strong> {{ scope.row.blockReason || '-' }}</div>
              <div><strong>解除条件:</strong> {{ scope.row.unblockCondition || '-' }}</div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" fixed="right">
          <template slot-scope="scope">
            <el-dropdown trigger="click" @command="(command) => handleCommand(command, scope.row)">
              <span class="el-dropdown-link">
                状态流转<i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="start" v-if="scope.row.status === 'NOT_STARTED'">开始</el-dropdown-item>
                <el-dropdown-item command="complete" v-if="scope.row.status === 'IN_PROGRESS'">完成</el-dropdown-item>
                <el-dropdown-item command="block" v-if="scope.row.status === 'IN_PROGRESS'">阻塞</el-dropdown-item>
                <el-dropdown-item command="unblock" v-if="scope.row.status === 'BLOCKED'">解除阻塞</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <el-button size="mini" @click="editTask(scope.row)">编辑</el-button>
            <el-button size="mini" @click="manageDependencies(scope.row)">管理依赖</el-button>
            <el-button size="mini" type="danger" @click="deleteTask(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input type="textarea" v-model="form.description" :rows="2"></el-input>
        </el-form-item>
        <el-form-item label="负责人" prop="assignee">
          <el-input v-model="form.assignee"></el-input>
        </el-form-item>
        <el-form-item label="预计工期(天)" prop="estimatedDays">
          <el-input-number v-model="form.estimatedDays" :min="1" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="计划开始日期" prop="plannedStartDate">
          <el-date-picker v-model="form.plannedStartDate" type="date" placeholder="选择日期" style="width: 100%;" format="yyyy-MM-dd" value-format="yyyy-MM-dd"></el-date-picker>
        </el-form-item>
        <el-form-item label="所属项目" prop="projectId" v-if="projects.length > 0">
          <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%;">
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="前置依赖">
          <el-select v-model="form.dependencyIds" multiple placeholder="选择前置任务" style="width: 100%;">
            <el-option v-for="task in availableTasksForForm" :key="task.id" :label="task.name" :value="task.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="阻塞原因" prop="blockReason" v-if="isEdit && form.status === 'BLOCKED'">
          <el-input type="textarea" v-model="form.blockReason" :rows="2"></el-input>
        </el-form-item>
        <el-form-item label="解除条件" prop="unblockCondition" v-if="isEdit && form.status === 'BLOCKED'">
          <el-input type="textarea" v-model="form.unblockCondition" :rows="2"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveTask">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="阻塞任务" :visible.sync="blockDialogVisible" width="500px">
      <el-form :model="blockForm" :rules="blockRules" ref="blockForm" label-width="100px">
        <el-form-item label="阻塞原因" prop="reason">
          <el-input type="textarea" v-model="blockForm.reason" :rows="3"></el-input>
        </el-form-item>
        <el-form-item label="解除条件" prop="unblockCondition">
          <el-input type="textarea" v-model="blockForm.unblockCondition" :rows="3"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="blockDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmBlock">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="管理依赖" :visible.sync="dependencyDialogVisible" width="600px">
      <span>当前任务: {{ currentTask ? currentTask.name : '' }}</span>
      <el-checkbox-group v-model="selectedDependencies" style="margin-top: 15px; display: block;">
        <el-checkbox v-for="task in availableTasks" :key="task.id" :label="task.id" style="display: block; margin: 10px 0;">
          {{ task.name }} ({{ getStatusText(task.status) }})
        </el-checkbox>
      </el-checkbox-group>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dependencyDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveDependencies">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'TaskList',
  data() {
    return {
      tasks: [],
      projects: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '新建任务',
      isEdit: false,
      currentTask: null,
      blockDialogVisible: false,
      dependencyDialogVisible: false,
      selectedDependencies: [],
      showBlockedInfo: true,
      form: {
        id: null,
        name: '',
        description: '',
        assignee: '',
        estimatedDays: 1,
        plannedStartDate: '',
        projectId: null,
        dependencyIds: []
      },
      blockForm: {
        taskId: null,
        reason: '',
        unblockCondition: ''
      },
      rules: {
        name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
        assignee: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
      },
      blockRules: {
        reason: [{ required: true, message: '请输入阻塞原因', trigger: 'blur' }],
        unblockCondition: [{ required: true, message: '请输入解除条件', trigger: 'blur' }]
      }
    }
  },
  computed: {
    currentProjectName() {
      return this.$route.query.projectName
    },
    currentProjectId() {
      return this.$route.query.projectId
    },
    availableTasks() {
      if (!this.currentTask) return []
      return this.tasks.filter(t => t.id !== this.currentTask.id)
    },
    availableTasksForForm() {
      if (!this.form.id) {
        return this.tasks
      }
      return this.tasks.filter(t => t.id !== this.form.id)
    }
  },
  mounted() {
    this.loadProjects()
    this.loadTasks()
  },
  methods: {
    async loadProjects() {
      try {
        const res = await api.getProjects()
        this.projects = res.data
      } catch (error) {
        this.$message.error('加载项目失败')
      }
    },
    async loadTasks() {
      this.loading = true
      try {
        let res
        if (this.currentProjectId) {
          res = await api.getTasksByProject(this.currentProjectId)
        } else {
          res = await api.getTasks()
        }
        this.tasks = res.data
      } catch (error) {
        this.$message.error('加载任务失败')
      } finally {
        this.loading = false
      }
    },
    showCreateDialog() {
      this.isEdit = false
      this.dialogTitle = '新建任务'
      this.form = {
        id: null,
        name: '',
        description: '',
        assignee: '',
        estimatedDays: 1,
        plannedStartDate: '',
        projectId: this.currentProjectId || null,
        dependencyIds: []
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    editTask(task) {
      this.isEdit = true
      this.dialogTitle = '编辑任务'
      this.form = {
        id: task.id,
        name: task.name,
        description: task.description,
        assignee: task.assignee,
        estimatedDays: task.estimatedDays,
        plannedStartDate: task.plannedStartDate,
        projectId: task.projectId,
        dependencyIds: [...(task.dependencyIds || [])],
        status: task.status,
        blockReason: task.blockReason,
        unblockCondition: task.unblockCondition
      }
      this.dialogVisible = true
    },
    async saveTask() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            let task
            if (this.isEdit) {
              await api.updateTask(this.form.id, this.form)
              task = this.form
              // 编辑时：先移除旧依赖，再添加新依赖
              const oldTask = this.tasks.find(t => t.id === this.form.id)
              const oldDeps = oldTask ? oldTask.dependencyIds || [] : []
              const newDeps = this.form.dependencyIds || []
              const toRemove = oldDeps.filter(id => !newDeps.includes(id))
              const toAdd = newDeps.filter(id => !oldDeps.includes(id))
              for (const depId of toRemove) {
                await api.removeDependency(task.id, depId)
              }
              for (const depId of toAdd) {
                await api.addDependency(task.id, depId)
              }
              // 如果是阻塞状态，更新阻塞信息
              if (this.form.status === 'BLOCKED') {
                await api.blockTask(task.id, this.form.blockReason, this.form.unblockCondition)
              }
              this.$message.success('更新成功')
            } else {
              const res = await api.createTask(this.form)
              task = res.data
              // 创建时：直接添加所有依赖
              if (this.form.dependencyIds && this.form.dependencyIds.length > 0) {
                for (const depId of this.form.dependencyIds) {
                  await api.addDependency(task.id, depId)
                }
              }
              this.$message.success('创建成功')
            }
            this.dialogVisible = false
            this.loadTasks()
          } catch (error) {
            this.$message.error('保存失败')
          }
        }
      })
    },
    deleteTask(task) {
      this.$confirm('确定要删除该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await api.deleteTask(task.id)
          this.$message.success('删除成功')
          this.loadTasks()
        } catch (error) {
          this.$message.error('删除失败')
        }
      })
    },
    async handleCommand(command, task) {
      try {
        switch (command) {
          case 'start':
            await api.startTask(task.id)
            this.$message.success('任务已开始')
            break
          case 'complete':
            await api.completeTask(task.id)
            this.$message.success('任务已完成')
            break
          case 'block':
            this.currentTask = task
            this.blockForm = { taskId: task.id, reason: '', unblockCondition: '' }
            this.blockDialogVisible = true
            return
          case 'unblock':
            await api.unblockTask(task.id)
            this.$message.success('已解除阻塞')
            break
        }
        this.loadTasks()
      } catch (error) {
        this.$message.error(error.response ? error.response.data : '操作失败')
      }
    },
    async confirmBlock() {
      this.$refs.blockForm.validate(async (valid) => {
        if (valid) {
          try {
            await api.blockTask(this.blockForm.taskId, this.blockForm.reason, this.blockForm.unblockCondition)
            this.$message.success('任务已阻塞')
            this.blockDialogVisible = false
            this.loadTasks()
          } catch (error) {
            this.$message.error('操作失败')
          }
        }
      })
    },
    manageDependencies(task) {
      this.currentTask = task
      this.selectedDependencies = [...(task.dependencyIds || [])]
      this.dependencyDialogVisible = true
    },
    async saveDependencies() {
      try {
        const currentDeps = this.currentTask.dependencyIds || []
        const toAdd = this.selectedDependencies.filter(id => !currentDeps.includes(id))
        const toRemove = currentDeps.filter(id => !this.selectedDependencies.includes(id))
        
        for (const depId of toAdd) {
          await api.addDependency(this.currentTask.id, depId)
        }
        for (const depId of toRemove) {
          await api.removeDependency(this.currentTask.id, depId)
        }
        
        this.$message.success('依赖更新成功')
        this.dependencyDialogVisible = false
        this.loadTasks()
      } catch (error) {
        this.$message.error('更新失败')
      }
    },
    async checkDelayedTasks() {
      try {
        await api.checkDelayedTasks()
        this.$message.success('检查完成')
        this.loadTasks()
      } catch (error) {
        this.$message.error('检查失败')
      }
    },
    getStatusType(status) {
      const map = {
        NOT_STARTED: 'info',
        IN_PROGRESS: 'primary',
        BLOCKED: 'warning',
        COMPLETED: 'success',
        DELAYED: 'danger'
      }
      return map[status] || 'info'
    },
    getStatusText(status) {
      const map = {
        NOT_STARTED: '未开始',
        IN_PROGRESS: '进行中',
        BLOCKED: '阻塞',
        COMPLETED: '已完成',
        DELAYED: '延期'
      }
      return map[status] || status
    },
    getTaskName(taskId) {
      const task = this.tasks.find(t => t.id === taskId)
      return task ? task.name : taskId
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
}
</style>
