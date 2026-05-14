import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import ProjectList from '../views/ProjectList.vue'
import TaskList from '../views/TaskList.vue'
import MemberLoadList from '../views/MemberLoadList.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/projects',
    name: 'ProjectList',
    component: ProjectList
  },
  {
    path: '/tasks',
    name: 'TaskList',
    component: TaskList
  },
  {
    path: '/member-loads',
    name: 'MemberLoadList',
    component: MemberLoadList
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

export default router
