package com.taskmanager.service;

import com.taskmanager.model.Project;
import com.taskmanager.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    @Autowired
    private DataStorage dataStorage;

    public Project createProject(Project project) {
        String id = UUID.randomUUID().toString();
        project.setId(id);
        project.setCreatedAt(LocalDate.now());
        project.setUpdatedAt(LocalDate.now());
        if (project.getTaskIds() == null) {
            project.setTaskIds(new ArrayList<>());
        }
        dataStorage.getProjects().put(id, project);
        return project;
    }

    public Project getProjectById(String id) {
        return dataStorage.getProjects().get(id);
    }

    public List<Project> getAllProjects() {
        return new ArrayList<>(dataStorage.getProjects().values());
    }

    public Project updateProject(String id, Project project) {
        Project existing = dataStorage.getProjects().get(id);
        if (existing == null) {
            return null;
        }
        existing.setName(project.getName());
        existing.setDescription(project.getDescription());
        existing.setStartDate(project.getStartDate());
        existing.setEndDate(project.getEndDate());
        existing.setUpdatedAt(LocalDate.now());
        return existing;
    }

    public boolean deleteProject(String id) {
        return dataStorage.getProjects().remove(id) != null;
    }

    public void addTaskToProject(String projectId, String taskId) {
        Project project = dataStorage.getProjects().get(projectId);
        if (project != null) {
            project.getTaskIds().add(taskId);
            project.setUpdatedAt(LocalDate.now());
        }
    }

    public void removeTaskFromProject(String projectId, String taskId) {
        Project project = dataStorage.getProjects().get(projectId);
        if (project != null) {
            project.getTaskIds().remove(taskId);
            project.setUpdatedAt(LocalDate.now());
        }
    }
}
