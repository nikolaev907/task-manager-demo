package com.demo.taskmanager.kafka;

import com.demo.taskmanager.model.Notification;
import com.demo.taskmanager.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification(
                "Task created",
                "Test Task",
                TaskStatus.PENDING,
                LocalDateTime.now()
        );
    }

    @Test
    void consumeNotification_ShouldLogNotification() {
        kafkaConsumerService.consumeNotification(testNotification);
    }

    @Test
    void consumeNotification_WithNullNotification_ShouldLogWithNull() {
        kafkaConsumerService.consumeNotification(null);
    }
}
