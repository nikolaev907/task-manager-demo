package com.demo.taskmanager.service;

import com.demo.taskmanager.TaskFactory;
import com.demo.taskmanager.model.Notification;
import com.demo.taskmanager.model.Task;
import com.demo.taskmanager.model.TaskStatus;
import com.demo.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = TaskFactory.createDefaultTask();
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        List<Task> tasks = Collections.singletonList(sampleTask);
        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Task");
        verify(taskRepository).findAll();
    }

    @Test
    void getTaskById_WhenExists_ShouldReturnTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        Task result = taskService.getTaskById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(taskRepository).findById(1L);
    }

    @Test
    void getTaskById_WhenNotExists_ShouldThrowException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Task not found");

        verify(taskRepository).findById(99L);
    }

    @Test
    void createTask_ShouldSaveAndSendNotification() {
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");

        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);
        ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
        when(kafkaTemplate.send(eq("task-notifications"), any(Notification.class)))
                .thenReturn(future);
        doNothing().when(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(Notification.class));

        Task result = taskService.createTask(newTask);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
        verify(taskRepository).save(any(Task.class));
        verify(kafkaTemplate).send(eq("task-notifications"), any(Notification.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(Notification.class));
    }

    @Test
    void updateTask_ShouldUpdateAndSendNotification() {
        Task updatedDetails = new Task();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);
        ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
        when(kafkaTemplate.send(eq("task-notifications"), any(Notification.class)))
                .thenReturn(future);
        doNothing().when(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(Notification.class));

        Task result = taskService.updateTask(1L, updatedDetails);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_WhenOnlyStatusChanged_ShouldUpdateStatus() {
        Task updatedDetails = new Task();
        updatedDetails.setStatus(TaskStatus.COMPLETED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);
        ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
        when(kafkaTemplate.send(eq("task-notifications"), any(Notification.class)))
                .thenReturn(future);
        doNothing().when(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(Notification.class));

        Task result = taskService.updateTask(1L, updatedDetails);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void deleteTask_ShouldDeleteAndSendNotification() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        doNothing().when(taskRepository).deleteById(1L);
        ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
        when(kafkaTemplate.send(eq("task-notifications"), any(Notification.class)))
                .thenReturn(future);
        doNothing().when(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(Notification.class));

        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
        verify(kafkaTemplate).send(eq("task-notifications"), any(Notification.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(Notification.class));
    }

    @Test
    void updateTaskStatus_ShouldUpdateAndSendNotification() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);
        ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
        when(kafkaTemplate.send(eq("task-notifications"), any(Notification.class)))
                .thenReturn(future);
        doNothing().when(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(Notification.class));

        Task result = taskService.updateTaskStatus(1L, TaskStatus.COMPLETED);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getTasksByStatus_ShouldReturnFilteredTasks() {
        List<Task> tasks = Collections.singletonList(sampleTask);
        when(taskRepository.findByStatus(TaskStatus.PENDING)).thenReturn(tasks);

        List<Task> result = taskService.getTasksByStatus(TaskStatus.PENDING);

        assertThat(result).hasSize(1);
        verify(taskRepository).findByStatus(TaskStatus.PENDING);
    }

    @Test
    void sendNotification_WhenKafkaFails_ShouldNotThrowException() {
        Task newTask = new Task();
        newTask.setTitle("Test");

        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(eq("task-notifications"), any(Notification.class));

        Task result = taskService.createTask(newTask);

        assertThat(result).isNotNull();
        verify(kafkaTemplate).send(eq("task-notifications"), any(Notification.class));
    }
}
