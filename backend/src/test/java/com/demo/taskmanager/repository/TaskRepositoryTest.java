package com.demo.taskmanager.repository;

import com.demo.taskmanager.TaskFactory;
import com.demo.taskmanager.model.Task;
import com.demo.taskmanager.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private Task pendingTask;

    @BeforeEach
    void setUp() {
        pendingTask = new Task();
        pendingTask.setTitle("This is a pending task");
        pendingTask.setDescription("Pending Description");
        pendingTask.setStatus(TaskStatus.PENDING);
        pendingTask.setCreatedAt(LocalDateTime.now());

        Task completedTask = new Task();
        completedTask.setTitle("This task is completed");
        completedTask.setDescription("Completed Description");
        completedTask.setStatus(TaskStatus.COMPLETED);
        completedTask.setCreatedAt(LocalDateTime.now());

        entityManager.persist(pendingTask);
        entityManager.persist(completedTask);
        entityManager.flush();
    }

    @Test
    void findByStatus_ShouldReturnTasksWithGivenStatus() {
        List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING);
        List<Task> completedTasks = taskRepository.findByStatus(TaskStatus.COMPLETED);

        assertThat(pendingTasks).hasSize(1);
        assertThat(pendingTasks.get(0).getTitle()).isEqualTo("This is a pending task");

        assertThat(completedTasks).hasSize(1);
        assertThat(completedTasks.get(0).getTitle()).isEqualTo("This task is completed");
    }

    @Test
    void findByStatus_WhenNoTasks_ShouldReturnEmptyList() {
        List<Task> inProgressTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS);

        assertThat(inProgressTasks).isEmpty();
    }

    @Test
    void save_ShouldPersistTask() {
        Task newTask = TaskFactory.createDefaultTask();

        Task savedTask = taskRepository.save(newTask);

        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo("Test Task");
    }

    @Test
    void findById_ShouldReturnTask() {
        Task found = taskRepository.findById(pendingTask.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("This is a pending task");
    }

    @Test
    void deleteById_ShouldRemoveTask() {
        taskRepository.deleteById(pendingTask.getId());

        Task found = taskRepository.findById(pendingTask.getId()).orElse(null);
        assertThat(found).isNull();
    }

    @Test
    void preUpdate_ShouldUpdateUpdatedAt() throws InterruptedException {
        Thread.sleep(10);
        pendingTask.setTitle("Updated Title");
        entityManager.persistAndFlush(pendingTask);

        assertThat(pendingTask.getUpdatedAt()).isNotNull();
    }
}
