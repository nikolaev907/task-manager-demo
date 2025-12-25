package com.demo.taskmanager.service;

import com.demo.taskmanager.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "task-notifications", groupId = "task-group")
    public void consumeNotification(Notification notification) {
        log.info("Received notification from Kafka: {}", notification);

        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}
