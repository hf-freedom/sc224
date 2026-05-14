<template>
  <div id="app">
    <el-container>
      <el-header style="background: #409EFF; color: white; padding: 0 20px;">
        <h2 style="margin: 0; line-height: 60px;">项目任务依赖管理系统</h2>
      </el-header>
      <el-container>
        <el-aside width="200px" style="background: #f5f5f5;">
          <el-menu router :default-active="$route.path" style="border: none;">
            <el-menu-item index="/">
              <i class="el-icon-s-home"></i>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/projects">
              <i class="el-icon-folder-opened"></i>
              <span>项目管理</span>
            </el-menu-item>
            <el-menu-item index="/tasks">
              <i class="el-icon-notebook-2"></i>
              <span>任务管理</span>
            </el-menu-item>
            <el-menu-item index="/member-loads">
              <i class="el-icon-user"></i>
              <span>成员负载</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main style="background: #fff; padding: 20px;">
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  min-height: 100vh;
}
.el-header {
  height: 60px !important;
}
</style>
