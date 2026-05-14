package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:3001")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task created = taskService.createTask(task);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        Task task = taskService.getTaskById(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getTasksByProjectId(@PathVariable String projectId) {
        return ResponseEntity.ok(taskService.getTasksByProjectId(projectId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable String id, @RequestBody Task task) {
        Task updated = taskService.updateTask(id, task);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        boolean deleted = taskService.deleteTask(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/dependencies/{dependencyId}")
    public ResponseEntity<Void> addDependency(@PathVariable String taskId, @PathVariable String dependencyId) {
        taskService.addDependency(taskId, dependencyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}/dependencies/{dependencyId}")
    public ResponseEntity<Void> removeDependency(@PathVariable String taskId, @PathVariable String dependencyId) {
        taskService.removeDependency(taskId, dependencyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/can-start")
    public ResponseEntity<Boolean> canStartTask(@PathVariable String id) {
        return ResponseEntity.ok(taskService.canStartTask(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startTask(@PathVariable String id) {
        try {
            Task task = taskService.startTask(id);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(task);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable String id) {
        Task task = taskService.completeTask(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Task> blockTask(@PathVariable String id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        String unblockCondition = body.get("unblockCondition");
        Task task = taskService.blockTask(id, reason, unblockCondition);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<Task> unblockTask(@PathVariable String id) {
        Task task = taskService.unblockTask(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/upcoming/{days}")
    public ResponseEntity<List<Task>> getUpcomingDeadlineTasks(@PathVariable int days) {
        return ResponseEntity.ok(taskService.getUpcomingDeadlineTasks(days));
    }

    @PostMapping("/check-delayed")
    public ResponseEntity<Void> checkDelayedTasks() {
        taskService.checkDelayedTasks();
        return ResponseEntity.ok().build();
    }
}
