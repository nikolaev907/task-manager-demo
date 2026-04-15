package com.demo.taskmanager.integration;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.demo.taskmanager.kafka.KafkaConsumerService;
import com.demo.taskmanager.model.Notification;
import com.demo.taskmanager.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka()
@ActiveProfiles("test")
class RealKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private KafkaConsumerService consumerService;

    private TestAppender testAppender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(KafkaConsumerService.class);
        testAppender = new TestAppender();
        testAppender.start();
        logger.addAppender(testAppender);
    }

    @Test
    void testRealKafkaMessage() {
        Notification notification = new Notification("Test", "Task", TaskStatus.PENDING, LocalDateTime.now());
        kafkaTemplate.send("task-notifications", notification);
        consumerService.consumeNotification(notification);

        assertThat(testAppender.getEvents())
                .extracting(ILoggingEvent::getMessage)
                .containsExactly("Received notification from Kafka: {}");
    }

    static class TestAppender extends AppenderBase<ILoggingEvent> {
        private final List<ILoggingEvent> events = new ArrayList<>();

        @Override
        protected void append(ILoggingEvent event) {
            events.add(event);
        }

        public List<ILoggingEvent> getEvents() {
            return new ArrayList<>(events);
        }
    }
}
