package com.demo.taskmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private String message;
    private String taskTitle;
    private TaskStatus status;
    private LocalDateTime timestamp = LocalDateTime.now();
    private LocalDateTime updateAt;

    public Notification(String message, String taskTitle, TaskStatus status, LocalDateTime updateAt) {
        this.message = message;
        this.taskTitle = taskTitle;
        this.status = status;
        this.updateAt = updateAt;
    }
}
