package com.taskmanager.storage;

import com.taskmanager.model.Project;
import com.taskmanager.model.Task;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataStorage {

    private final Map<String, Project> projects = new ConcurrentHashMap<>();
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();

    public Map<String, Project> getProjects() {
        return projects;
    }

    public Map<String, Task> getTasks() {
        return tasks;
    }

    public List<Task> getTasksByProjectId(String projectId) {
        List<Task> projectTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (projectId.equals(task.getProjectId())) {
                projectTasks.add(task);
            }
        }
        return projectTasks;
    }

    public List<Task> getTasksByAssignee(String assignee) {
        List<Task> assigneeTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (assignee.equals(task.getAssignee())) {
                assigneeTasks.add(task);
            }
        }
        return assigneeTasks;
    }
}
