package com.demo.taskmanager.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {
    private String message;
    private String taskTitle;
    private TaskStatus status;
    private LocalDateTime timestamp = LocalDateTime.now();

    public Notification(String message, String taskTitle, TaskStatus status, LocalDateTime updateAt) {
        this.message = message;
        this.taskTitle = taskTitle;
        this.status = status;
    }
}
