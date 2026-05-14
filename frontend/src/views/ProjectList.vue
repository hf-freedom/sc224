<template>
  <div class="project-list">
    <el-card>
      <div slot="header" class="card-header">
        <span>项目列表</span>
        <el-button type="primary" size="small" @click="showCreateDialog">新建项目</el-button>
      </div>
      <el-table :data="projects" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="项目名称" width="200"></el-table-column>
        <el-table-column prop="description" label="项目描述" show-overflow-tooltip></el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120"></el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="120"></el-table-column>
        <el-table-column label="任务数量" width="100">
          <template slot-scope="scope">
            {{ scope.row.taskIds ? scope.row.taskIds.length : 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewTasks(scope.row)">查看任务</el-button>
            <el-button size="mini" @click="editProject(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="deleteProject(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="80px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input type="textarea" v-model="form.description" :rows="3"></el-input>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期" style="width: 100%;" format="yyyy-MM-dd" value-format="yyyy-MM-dd"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择日期" style="width: 100%;" format="yyyy-MM-dd" value-format="yyyy-MM-dd"></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveProject">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'ProjectList',
  data() {
    return {
      projects: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '新建项目',
      isEdit: false,
      form: {
        id: null,
        name: '',
        description: '',
        startDate: '',
        endDate: ''
      },
      rules: {
        name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadProjects()
  },
  methods: {
    async loadProjects() {
      this.loading = true
      try {
        const res = await api.getProjects()
        this.projects = res.data
      } catch (error) {
        this.$message.error('加载项目失败')
      } finally {
        this.loading = false
      }
    },
    showCreateDialog() {
      this.isEdit = false
      this.dialogTitle = '新建项目'
      this.form = {
        id: null,
        name: '',
        description: '',
        startDate: '',
        endDate: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    editProject(project) {
      this.isEdit = true
      this.dialogTitle = '编辑项目'
      this.form = {
        id: project.id,
        name: project.name,
        description: project.description,
        startDate: project.startDate,
        endDate: project.endDate
      }
      this.dialogVisible = true
    },
    async saveProject() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            if (this.isEdit) {
              await api.updateProject(this.form.id, this.form)
              this.$message.success('更新成功')
            } else {
              await api.createProject(this.form)
              this.$message.success('创建成功')
            }
            this.dialogVisible = false
            this.loadProjects()
          } catch (error) {
            this.$message.error('保存失败')
          }
        }
      })
    },
    deleteProject(project) {
      this.$confirm('确定要删除该项目吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await api.deleteProject(project.id)
          this.$message.success('删除成功')
          this.loadProjects()
        } catch (error) {
          this.$message.error('删除失败')
        }
      })
    },
    viewTasks(project) {
      this.$router.push({ path: '/tasks', query: { projectId: project.id, projectName: project.name } })
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
</style>
