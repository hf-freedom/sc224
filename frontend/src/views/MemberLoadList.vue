<template>
  <div class="member-load-list">
    <el-card>
      <div slot="header" class="card-header">
        <span>成员负载统计</span>
        <el-button type="primary" size="small" @click="loadMemberLoads">刷新</el-button>
      </div>
      <el-table :data="memberLoads" style="width: 100%" v-loading="loading">
        <el-table-column prop="memberName" label="成员姓名" width="150"></el-table-column>
        <el-table-column prop="taskCount" label="任务数量" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.overload ? 'danger' : ''" size="small">
              {{ scope.row.taskCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalEstimatedDays" label="总预计工期(天)" width="150"></el-table-column>
        <el-table-column label="负载状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.overload ? 'danger' : 'success'" size="small">
              {{ scope.row.overload ? '过载' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="负载率" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-progress :percentage="Math.min(scope.row.taskCount * 20, 100)" :stroke-width="15" 
                         :color="scope.row.overload ? '#F56C6C' : '#67C23A'">
            </el-progress>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-top: 20px;">
      <div slot="header">
        <span>负载警告</span>
      </div>
      <el-alert v-for="member in overloadedMembers" :key="member.memberName"
                :title="`成员 ${member.memberName} 当前负载过高，任务数量: ${member.taskCount} 个`"
                type="warning" show-icon style="margin-bottom: 10px;">
      </el-alert>
      <el-empty v-if="overloadedMembers.length === 0" description="所有成员负载正常"></el-empty>
    </el-card>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'MemberLoadList',
  data() {
    return {
      memberLoads: [],
      loading: false
    }
  },
  computed: {
    overloadedMembers() {
      return this.memberLoads.filter(m => m.overload)
    }
  },
  mounted() {
    this.loadMemberLoads()
  },
  methods: {
    async loadMemberLoads() {
      this.loading = true
      try {
        const res = await api.getMemberLoads()
        this.memberLoads = res.data
      } catch (error) {
        this.$message.error('加载成员负载失败')
      } finally {
        this.loading = false
      }
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
