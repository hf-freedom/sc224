package com.taskmanager.model;

public enum TaskStatus {
    NOT_STARTED("未开始"),
    IN_PROGRESS("进行中"),
    BLOCKED("阻塞"),
    COMPLETED("已完成"),
    DELAYED("延期");

    private String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
