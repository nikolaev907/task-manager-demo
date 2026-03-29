package com.demo.taskmanager.service;

import com.demo.taskmanager.model.Notification;
import com.demo.taskmanager.model.Task;
import com.demo.taskmanager.model.TaskStatus;
import com.demo.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final KafkaConsumerService kafkaConsumerService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(new Throwable().getLocalizedMessage());
                    return new RuntimeException("Task not found");
                });
    }

    @Transactional
    public Task createTask(Task task) {
        task.setStatus(TaskStatus.PENDING);
        Task savedTask = taskRepository.save(task);

        sendNotification("Task created", savedTask.getTitle(), TaskStatus.PENDING, task.getUpdatedAt());
        return savedTask;
    }

    @Transactional
    public Task updateTask(Long id, Task taskDetails) {
        Task task = getTaskById(id);
        task.setUpdatedAt(LocalDateTime.now());
        if (taskDetails.getTitle() != null) {
            task.setTitle(taskDetails.getTitle());
        }
        if (taskDetails.getDescription() != null) {
            task.setDescription(taskDetails.getDescription());
        }
        if (taskDetails.getStatus() != null && taskDetails.getStatus() != task.getStatus()) {
            task.setStatus(taskDetails.getStatus());
        }
        sendNotification("Task status updated", task.getTitle(), task.getStatus(), task.getUpdatedAt());

        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.deleteById(id);
        sendNotification("Task deleted", task.getTitle(), task.getStatus(), task.getUpdatedAt());
    }

    @Transactional
    public Task updateTaskStatus(Long id, TaskStatus status) {
        Task task = getTaskById(id);
        task.setStatus(status);
        LocalDateTime updatedAt = task.getUpdatedAt();
        Task updatedTask = taskRepository.save(task);

        sendNotification("Task status updated", task.getTitle(), status, updatedAt);
        return updatedTask;
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    private void sendNotification(String message, String taskTitle, TaskStatus status, LocalDateTime updatedAt) {
        Notification notification = new Notification(message, taskTitle, status, updatedAt);
        try {
            kafkaTemplate.send("task-notifications", notification);
            messagingTemplate.convertAndSend("/topic/notifications", notification);
        } catch (Exception e) {
            log.error("Notification send error: {}", e.getMessage());
        }

    }
}
