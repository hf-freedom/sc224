import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export default {
  getProjects() {
    return api.get('/projects')
  },
  getProject(id) {
    return api.get(`/projects/${id}`)
  },
  createProject(data) {
    return api.post('/projects', data)
  },
  updateProject(id, data) {
    return api.put(`/projects/${id}`, data)
  },
  deleteProject(id) {
    return api.delete(`/projects/${id}`)
  },
  
  getTasks() {
    return api.get('/tasks')
  },
  getTask(id) {
    return api.get(`/tasks/${id}`)
  },
  getTasksByProject(projectId) {
    return api.get(`/tasks/project/${projectId}`)
  },
  createTask(data) {
    return api.post('/tasks', data)
  },
  updateTask(id, data) {
    return api.put(`/tasks/${id}`, data)
  },
  deleteTask(id) {
    return api.delete(`/tasks/${id}`)
  },
  startTask(id) {
    return api.post(`/tasks/${id}/start`)
  },
  completeTask(id) {
    return api.post(`/tasks/${id}/complete`)
  },
  blockTask(id, reason, unblockCondition) {
    return api.post(`/tasks/${id}/block`, { reason, unblockCondition })
  },
  unblockTask(id) {
    return api.post(`/tasks/${id}/unblock`)
  },
  addDependency(taskId, dependencyId) {
    return api.post(`/tasks/${taskId}/dependencies/${dependencyId}`)
  },
  removeDependency(taskId, dependencyId) {
    return api.delete(`/tasks/${taskId}/dependencies/${dependencyId}`)
  },
  checkDelayedTasks() {
    return api.post('/tasks/check-delayed')
  },
  getUpcomingTasks(days) {
    return api.get(`/tasks/upcoming/${days}`)
  },
  
  getMemberLoads() {
    return api.get('/member-loads')
  },
  getMemberLoad(memberName) {
    return api.get(`/member-loads/${memberName}`)
  },
  getOverloadedMembers() {
    return api.get('/member-loads/overloaded')
  }
}
