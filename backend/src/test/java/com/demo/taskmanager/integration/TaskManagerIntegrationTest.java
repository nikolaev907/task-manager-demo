package com.demo.taskmanager.integration;

import com.demo.taskmanager.TaskFactory;
import com.demo.taskmanager.model.Task;
import com.demo.taskmanager.model.TaskStatus;
import com.demo.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskManagerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        when(kafkaTemplate.send(eq("task-notifications"), any()))
                .thenReturn(null);
        doNothing().when(kafkaTemplate).flush();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    }

    @Test
    void createAndGetTask_Integration() {
        Task task = TaskFactory.createTask(
                "Integration Test Task",
                "Integration Test Description",
                TaskStatus.PENDING
        );
        task.setCreatedAt(LocalDateTime.now());

        ResponseEntity<Task> createResponse = restTemplate.postForEntity(
                "/api/tasks",
                task,
                Task.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getId()).isNotNull();
        assertThat(createResponse.getBody().getTitle()).isEqualTo("Integration Test Task");

        Long taskId = createResponse.getBody().getId();

        ResponseEntity<Task> getResponse = restTemplate.getForEntity(
                "/api/tasks/" + taskId,
                Task.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(getResponse.getBody()).getTitle()).isEqualTo("Integration Test Task");
    }

    @Test
    void getAllTasks_Integration() {
        Task task1 = TaskFactory.createTask("Task 1", "Description 1", TaskStatus.PENDING);
        Task task2 = TaskFactory.createTask("Task 2", "Description 2", TaskStatus.COMPLETED);

        task1.setCreatedAt(LocalDateTime.now());
        task2.setCreatedAt(LocalDateTime.now());

        restTemplate.postForEntity("/api/tasks", task1, Task.class);
        restTemplate.postForEntity("/api/tasks", task2, Task.class);

        ResponseEntity<Task[]> response = restTemplate.getForEntity(
                "/api/tasks",
                Task[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getTaskById_WhenNotExists_ShouldReturn404() {
        ResponseEntity<Task> response = restTemplate.getForEntity(
                "/api/tasks/999",
                Task.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateTaskStatus_Integration() {
        Task task = TaskFactory.createTask("Update Status Task", "Description", TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());

        ResponseEntity<Task> createResponse = restTemplate.postForEntity(
                "/api/tasks",
                task,
                Task.class
        );

        Long taskId = Objects.requireNonNull(createResponse.getBody()).getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Task> updateResponse = restTemplate.exchange(
                "/api/tasks/" + taskId + "/status?status=COMPLETED",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                Task.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    void deleteTask_Integration() {
        Task task = TaskFactory.createTask("Delete Task", "Description", TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());

        ResponseEntity<Task> createResponse = restTemplate.postForEntity(
                "/api/tasks",
                task,
                Task.class
        );

        Long taskId = Objects.requireNonNull(createResponse.getBody()).getId();

        restTemplate.delete("/api/tasks/" + taskId);

        ResponseEntity<Task> getResponse = restTemplate.getForEntity(
                "/api/tasks/" + taskId,
                Task.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void filterByStatus_Integration() {
        Task pendingTask = TaskFactory.createPendingTask();
        Task completedTask = TaskFactory.createCompletedTask();

        pendingTask.setCreatedAt(LocalDateTime.now());
        completedTask.setCreatedAt(LocalDateTime.now());

        restTemplate.postForEntity("/api/tasks", pendingTask, Task.class);
        restTemplate.postForEntity("/api/tasks", completedTask, Task.class);

        ResponseEntity<Task[]> pendingResponse = restTemplate.getForEntity(
                "/api/tasks/status/PENDING",
                Task[].class
        );

        assertThat(pendingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pendingResponse.getBody()).hasSize(1);
        assertThat(pendingResponse.getBody()[0].getTitle()).isEqualTo("Pending Task");
    }

    @Test
    void updateTask_Integration() {
        Task task = TaskFactory.createTask("Original Task", "Original Description", TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());

        ResponseEntity<Task> createResponse = restTemplate.postForEntity(
                "/api/tasks",
                task,
                Task.class
        );

        Long taskId = Objects.requireNonNull(createResponse.getBody()).getId();

        Task updatedTask = TaskFactory.createTask(
                "Updated Task",
                "Updated Description",
                TaskStatus.IN_PROGRESS
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Task> updateResponse = restTemplate.exchange(
                "/api/tasks/" + taskId,
                HttpMethod.PUT,
                new HttpEntity<>(updatedTask, headers),
                Task.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(updateResponse.getBody()).getTitle()).isEqualTo("Updated Task");
        assertThat(updateResponse.getBody().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }
}
