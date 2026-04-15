package com.demo.taskmanager.controller;

import com.demo.taskmanager.TaskFactory;
import com.demo.taskmanager.model.Task;
import com.demo.taskmanager.model.TaskStatus;
import com.demo.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = TaskFactory.createDefaultTask();
    }

    @Test
    void getAllTasks_ShouldReturnTasksList() throws Exception {
        List<Task> tasks = Collections.singletonList(sampleTask);
        when(taskService.getAllTasks()).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(taskService).getAllTasks();
    }

    @Test
    void getTaskById_WhenExists_ShouldReturnTask() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(sampleTask);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));

        verify(taskService).getTaskById(1L);
    }

    @Test
    void getTaskById_WhenNotExists_ShouldReturn404() throws Exception {
        when(taskService.getTaskById(99L)).thenThrow(new RuntimeException("Task not found"));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound());

        verify(taskService).getTaskById(99L);
    }

    @Test
    void createTask_ShouldReturnCreatedTask() throws Exception {
        when(taskService.createTask(any(Task.class))).thenReturn(sampleTask);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));

        verify(taskService).createTask(any(Task.class));
    }

    @Test
    void updateTask_ShouldReturnUpdatedTask() throws Exception {
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Task");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);

        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(taskService).updateTask(eq(1L), any(Task.class));
    }

    @Test
    void deleteTask_ShouldReturnNoContent() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    void updateTaskStatus_ShouldReturnUpdatedTask() throws Exception {
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setStatus(TaskStatus.COMPLETED);

        when(taskService.updateTaskStatus(eq(1L), eq(TaskStatus.COMPLETED))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/1/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(taskService).updateTaskStatus(1L, TaskStatus.COMPLETED);
    }

    @Test
    void getTasksByStatus_ShouldReturnFilteredTasks() throws Exception {
        List<Task> tasks = Collections.singletonList(sampleTask);
        when(taskService.getTasksByStatus(TaskStatus.PENDING)).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(taskService).getTasksByStatus(TaskStatus.PENDING);
    }
}
