package com.demo.taskmanager;

import com.demo.taskmanager.model.Task;
import com.demo.taskmanager.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskFactory {

    private static Long idCounter = 1L;

    public static Task createDefaultTask() {
        return createTask(null, "Test Task", "Test Description", TaskStatus.PENDING);
    }

    public static Task createTask(String title, String description, TaskStatus status) {
        return createTask(null, title, description, status);
    }

    public static Task createTask(Long id, String title, String description, TaskStatus status) {
        Task task = new Task();
        task.setId(id != null ? id : idCounter++);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }

    public static Task createPendingTask() {
        return createTask("Pending Task", "This is a pending task", TaskStatus.PENDING);
    }

    public static Task createInProgressTask() {
        return createTask("In Progress Task", "This task is in progress", TaskStatus.IN_PROGRESS);
    }

    public static Task createCompletedTask() {
        return createTask("Completed Task", "This task is completed", TaskStatus.COMPLETED);
    }

    public static Task createCancelledTask() {
        return createTask("Cancelled Task", "This task was cancelled", TaskStatus.CANCELLED);
    }

    public static List<Task> createMultipleTasks(int count) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            tasks.add(createTask(
                    "Task " + i,
                    "Description for task " + i,
                    i % 2 == 0 ? TaskStatus.COMPLETED : TaskStatus.PENDING
            ));
        }
        return tasks;
    }

    public static Task createTaskWithUpdates(Task original, String newTitle, String newDescription, TaskStatus newStatus) {
        Task updated = new Task();
        updated.setId(original.getId());
        updated.setTitle(newTitle != null ? newTitle : original.getTitle());
        updated.setDescription(newDescription != null ? newDescription : original.getDescription());
        updated.setStatus(newStatus != null ? newStatus : original.getStatus());
        updated.setCreatedAt(original.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());
        return updated;
    }

    public static void resetIdCounter() {
        idCounter = 1L;
    }
}
