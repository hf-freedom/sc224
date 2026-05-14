package com.taskmanager.service;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private ProjectService projectService;

    public Task createTask(Task task) {
        String id = UUID.randomUUID().toString();
        task.setId(id);
        task.setStatus(TaskStatus.NOT_STARTED);
        task.setCreatedAt(LocalDate.now());
        task.setUpdatedAt(LocalDate.now());
        if (task.getDependencyIds() == null) {
            task.setDependencyIds(new ArrayList<>());
        }
        if (task.getPlannedStartDate() != null && task.getEstimatedDays() != null) {
            task.setPlannedEndDate(task.getPlannedStartDate().plusDays(task.getEstimatedDays()));
        }
        dataStorage.getTasks().put(id, task);
        if (task.getProjectId() != null) {
            projectService.addTaskToProject(task.getProjectId(), id);
        }
        return task;
    }

    public Task getTaskById(String id) {
        return dataStorage.getTasks().get(id);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(dataStorage.getTasks().values());
    }

    public List<Task> getTasksByProjectId(String projectId) {
        return dataStorage.getTasksByProjectId(projectId);
    }

    public Task updateTask(String id, Task task) {
        Task existing = dataStorage.getTasks().get(id);
        if (existing == null) {
            return null;
        }
        existing.setName(task.getName());
        existing.setDescription(task.getDescription());
        existing.setAssignee(task.getAssignee());
        existing.setEstimatedDays(task.getEstimatedDays());
        existing.setPlannedStartDate(task.getPlannedStartDate());
        if (task.getPlannedStartDate() != null && task.getEstimatedDays() != null) {
            existing.setPlannedEndDate(task.getPlannedStartDate().plusDays(task.getEstimatedDays()));
        }
        existing.setUpdatedAt(LocalDate.now());
        return existing;
    }

    public boolean deleteTask(String id) {
        Task task = dataStorage.getTasks().get(id);
        if (task == null) {
            return false;
        }
        if (task.getProjectId() != null) {
            projectService.removeTaskFromProject(task.getProjectId(), id);
        }
        return dataStorage.getTasks().remove(id) != null;
    }

    public void addDependency(String taskId, String dependencyId) {
        Task task = dataStorage.getTasks().get(taskId);
        if (task != null && !taskId.equals(dependencyId)) {
            task.getDependencyIds().add(dependencyId);
            task.setUpdatedAt(LocalDate.now());
        }
    }

    public void removeDependency(String taskId, String dependencyId) {
        Task task = dataStorage.getTasks().get(taskId);
        if (task != null) {
            task.getDependencyIds().remove(dependencyId);
            task.setUpdatedAt(LocalDate.now());
        }
    }

    public boolean canStartTask(String taskId) {
        Task task = dataStorage.getTasks().get(taskId);
        if (task == null) {
            return false;
        }
        for (String depId : task.getDependencyIds()) {
            Task dep = dataStorage.getTasks().get(depId);
            if (dep == null || dep.getStatus() != TaskStatus.COMPLETED) {
                return false;
            }
        }
        return true;
    }

    public Task startTask(String taskId) {
        if (!canStartTask(taskId)) {
            throw new IllegalStateException("前置任务未完成，无法开始任务");
        }
        Task task = dataStorage.getTasks().get(taskId);
        if (task == null) {
            return null;
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setActualStartDate(LocalDate.now());
        task.setUpdatedAt(LocalDate.now());
        return task;
    }

    public Task completeTask(String taskId) {
        Task task = dataStorage.getTasks().get(taskId);
        if (task == null) {
            return null;
        }
        task.setStatus(TaskStatus.COMPLETED);
        task.setActualEndDate(LocalDate.now());
        task.setUpdatedAt(LocalDate.now());
        return task;
    }

    public Task blockTask(String taskId, String reason, String unblockCondition) {
        Task task = dataStorage.getTasks().get(taskId);
        if (task == null) {
            return null;
        }
        task.setStatus(TaskStatus.BLOCKED);
        task.setBlockReason(reason);
        task.setUnblockCondition(unblockCondition);
        task.setUpdatedAt(LocalDate.now());
        return task;
    }

    public Task unblockTask(String taskId) {
        Task task = dataStorage.getTasks().get(taskId);
        if (task == null) {
            return null;
        }
        task.setStatus(TaskStatus.NOT_STARTED);
        task.setBlockReason(null);
        task.setUnblockCondition(null);
        task.setUpdatedAt(LocalDate.now());
        return task;
    }

    public void checkDelayedTasks() {
        LocalDate today = LocalDate.now();
        for (Task task : dataStorage.getTasks().values()) {
            if (task.getStatus() != TaskStatus.COMPLETED && 
                task.getPlannedEndDate() != null && 
                task.getPlannedEndDate().isBefore(today)) {
                if (task.getStatus() != TaskStatus.DELAYED) {
                    task.setStatus(TaskStatus.DELAYED);
                    task.setUpdatedAt(today);
                    // 计算延期天数并更新依赖任务
                    long delayDays = java.time.temporal.ChronoUnit.DAYS.between(task.getPlannedEndDate(), today);
                    LocalDate newStartDate = today.plusDays(1);
                    updateDependentTasksStartDate(task.getId(), newStartDate, new HashSet<>());
                }
            }
        }
    }

    private void updateDependentTasksStartDate(String delayedTaskId, LocalDate newStartDate, Set<String> visited) {
        if (visited.contains(delayedTaskId)) {
            return;
        }
        visited.add(delayedTaskId);
        
        for (Task task : dataStorage.getTasks().values()) {
            if (task.getDependencyIds().contains(delayedTaskId) && 
                task.getStatus() != TaskStatus.COMPLETED &&
                task.getPlannedStartDate() != null) {
                // 只更新还没开始的任务
                if (task.getStatus() == TaskStatus.NOT_STARTED) {
                    task.setPlannedStartDate(newStartDate);
                    if (task.getEstimatedDays() != null) {
                        task.setPlannedEndDate(task.getPlannedStartDate().plusDays(task.getEstimatedDays()));
                    }
                    task.setUpdatedAt(LocalDate.now());
                }
                // 递归更新后续依赖任务
                updateDependentTasksStartDate(task.getId(), 
                    task.getPlannedEndDate() != null ? task.getPlannedEndDate().plusDays(1) : newStartDate.plusDays(1), 
                    visited);
            }
        }
    }

    public List<Task> getUpcomingDeadlineTasks(int days) {
        List<Task> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        for (Task task : dataStorage.getTasks().values()) {
            if (task.getStatus() != TaskStatus.COMPLETED && 
                task.getPlannedEndDate() != null && 
                !task.getPlannedEndDate().isBefore(today) &&
                !task.getPlannedEndDate().isAfter(deadline)) {
                result.add(task);
            }
        }
        return result;
    }
}
