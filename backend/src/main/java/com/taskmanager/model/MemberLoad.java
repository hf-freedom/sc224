package com.taskmanager.model;

import lombok.Data;

@Data
public class MemberLoad {
    private String memberName;
    private Integer taskCount;
    private Integer totalEstimatedDays;
    private Boolean overload;
}
