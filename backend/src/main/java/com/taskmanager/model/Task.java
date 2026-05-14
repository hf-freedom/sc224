package com.taskmanager.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Task {
    private String id;
    private String projectId;
    private String name;
    private String description;
    private String assignee;
    private Integer estimatedDays;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private TaskStatus status;
    private List<String> dependencyIds = new ArrayList<>();
    private String blockReason;
    private String unblockCondition;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
