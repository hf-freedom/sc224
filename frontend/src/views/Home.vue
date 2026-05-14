<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div slot="header">
            <span>项目总数</span>
          </div>
          <div class="stat-number">{{ projectCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div slot="header">
            <span>任务总数</span>
          </div>
          <div class="stat-number">{{ taskCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div slot="header">
            <span>延期任务</span>
          </div>
          <div class="stat-number delayed">{{ delayedCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div slot="header">
            <span>负载过高成员</span>
          </div>
          <div class="stat-number overload">{{ overloadedCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>即将到期任务（3天内）</span>
            <el-button style="float: right; padding: 3px 0;" type="text" @click="refreshUpcoming">刷新</el-button>
          </div>
          <el-table :data="upcomingTasks" style="width: 100%" size="small">
            <el-table-column prop="name" label="任务名称" width="150"></el-table-column>
            <el-table-column prop="assignee" label="负责人" width="100"></el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="plannedEndDate" label="截止日期" width="120"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>任务状态分布</span>
          </div>
          <el-table :data="statusStats" style="width: 100%" size="small">
            <el-table-column prop="status" label="状态" width="120">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.statusKey)" size="small">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="数量" width="100"></el-table-column>
            <el-table-column prop="percentage" label="占比">
              <template slot-scope="scope">
                <el-progress :percentage="scope.row.percentage" :stroke-width="10" :show-text="true"></el-progress>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <div slot="header">
            <span>延期任务及依赖影响</span>
            <el-button type="primary" size="mini" style="float: right;" @click="checkDelayedTasks">执行延期检查并重算</el-button>
          </div>
          <el-table :data="delayedTasksWithImpact" style="width: 100%" size="small" row-key="id">
            <el-table-column type="expand">
              <template slot-scope="scope">
                <div v-if="scope.row.affectedTasks && scope.row.affectedTasks.length > 0">
                  <h4 style="margin: 10px 0; color: #F56C6C;">受影响的依赖任务（预计开始时间已重算）：</h4>
                  <el-table :data="scope.row.affectedTasks" style="width: 100%; margin-left: 20px;" size="mini">
                    <el-table-column prop="name" label="任务名称" width="180"></el-table-column>
                    <el-table-column prop="assignee" label="负责人" width="100"></el-table-column>
                    <el-table-column label="时间变更" width="250">
                      <template slot-scope="subScope">
                        <span style="color: #67C23A;">原开始: {{ subScope.row.originalStartDate }}</span>
                        <span style="color: #F56C6C; margin-left: 10px;">→ 新开始: {{ subScope.row.newStartDate }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="plannedEndDate" label="新结束日期" width="120"></el-table-column>
                  </el-table>
                </div>
                <div v-else style="padding: 10px; color: #909399;">此任务没有后续依赖任务</div>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="延期任务名称" width="200">
              <template slot-scope="scope">
                <span style="color: #F56C6C; font-weight: bold;">{{ scope.row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="assignee" label="负责人" width="100"></el-table-column>
            <el-table-column prop="plannedStartDate" label="原计划开始" width="120"></el-table-column>
            <el-table-column prop="plannedEndDate" label="原计划结束" width="120"></el-table-column>
            <el-table-column label="延期天数" width="100" align="center">
              <template slot-scope="scope">
                <el-tag type="danger" size="small">{{ scope.row.delayDays }}天</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="affectedCount" label="影响任务数" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.affectedCount > 0 ? 'warning' : 'info'" size="small">
                  {{ scope.row.affectedCount }}个
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="delayedTasksWithImpact.length === 0" description="暂无延期任务"></el-empty>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'Home',
  data() {
    return {
      projectCount: 0,
      taskCount: 0,
      delayedCount: 0,
      overloadedCount: 0,
      upcomingTasks: [],
      statusStats: [],
      allTasks: [],
      delayedTasksWithImpact: [],
      taskOriginalDates: {} // 保存任务原来的日期用于对比
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const [projectsRes, tasksRes, overloadedRes, upcomingRes] = await Promise.all([
          api.getProjects(),
          api.getTasks(),
          api.getOverloadedMembers(),
          api.getUpcomingTasks(3)
        ])
        
        this.projectCount = projectsRes.data.length
        this.allTasks = tasksRes.data
        this.taskCount = this.allTasks.length
        this.overloadedCount = overloadedRes.data.length
        this.upcomingTasks = upcomingRes.data
        
        this.calculateStatusStats()
        this.calculateDelayedCount()
        this.calculateDelayedImpact()
      } catch (error) {
        this.$message.error('加载数据失败')
      }
    },
    calculateStatusStats() {
      const statusMap = {}
      this.allTasks.forEach(task => {
        statusMap[task.status] = (statusMap[task.status] || 0) + 1
      })
      
      this.statusStats = Object.keys(statusMap).map(key => ({
        statusKey: key,
        status: this.getStatusText(key),
        count: statusMap[key],
        percentage: Math.round((statusMap[key] / this.taskCount) * 100)
      }))
    },
    calculateDelayedCount() {
      this.delayedCount = this.allTasks.filter(t => t.status === 'DELAYED').length
    },
    calculateDelayedImpact() {
      const delayedTasks = this.allTasks.filter(t => t.status === 'DELAYED')
      const today = new Date()
      
      this.delayedTasksWithImpact = delayedTasks.map(task => {
        // 计算延期天数
        let delayDays = 0
        if (task.plannedEndDate) {
          const endDate = new Date(task.plannedEndDate)
          delayDays = Math.ceil((today - endDate) / (1000 * 60 * 60 * 24))
        }
        
        // 查找受影响的依赖任务
        const affectedTasks = []
        this.findAffectedTasks(task.id, affectedTasks, task)
        
        return {
          ...task,
          delayDays: Math.max(delayDays, 0),
          affectedCount: affectedTasks.length,
          affectedTasks: affectedTasks
        }
      })
    },
    findAffectedTasks(delayedTaskId, result, sourceTask, level = 0, visited = new Set()) {
      if (visited.has(delayedTaskId)) return
      visited.add(delayedTaskId)
      
      this.allTasks.forEach(task => {
        if (task.dependencyIds && task.dependencyIds.includes(delayedTaskId) && task.status !== 'COMPLETED') {
          // 获取原始日期
          const originalStart = this.getOriginalStartDate(task.id, task.plannedStartDate)
          
          result.push({
            id: task.id,
            name: task.name,
            assignee: task.assignee,
            originalStartDate: originalStart,
            newStartDate: task.plannedStartDate,
            plannedEndDate: task.plannedEndDate,
            level: level
          })
          
          // 递归查找后续依赖
          this.findAffectedTasks(task.id, result, sourceTask, level + 1, visited)
        }
      })
    },
    getOriginalStartDate(taskId, currentDate) {
      // 简单模拟：如果有保存的原始日期就用，没有就用当前日期作为参考
      // 实际应用中应该从后端获取原始计划日期
      if (this.taskOriginalDates[taskId]) {
        return this.taskOriginalDates[taskId]
      }
      // 如果是延期任务的后续任务，计算可能的原始日期
      // 简单处理：用当前日期作为参考
      return currentDate || '-'
    },
    async checkDelayedTasks() {
      try {
        // 先保存当前的日期作为原始参考
        this.allTasks.forEach(task => {
          if (!this.taskOriginalDates[task.id]) {
            this.taskOriginalDates[task.id] = task.plannedStartDate
          }
        })
        
        await api.checkDelayedTasks()
        this.$message.success('延期检查完成，已自动重算依赖任务的预计开始时间')
        
        // 重新加载数据
        await this.loadData()
      } catch (error) {
        this.$message.error('延期检查失败')
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
    refreshUpcoming() {
      this.loadData()
    }
  }
}
</script>

<style scoped>
.stat-card {
  text-align: center;
}
.stat-number {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
}
.stat-number.delayed {
  color: #F56C6C;
}
.stat-number.overload {
  color: #E6A23C;
}
</style>
