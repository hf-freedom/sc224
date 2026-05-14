package com.taskmanager.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Project {
    private String id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> taskIds = new ArrayList<>();
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
