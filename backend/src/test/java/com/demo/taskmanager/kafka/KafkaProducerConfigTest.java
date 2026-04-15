package com.demo.taskmanager.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class KafkaProducerConfigTest {
    private KafkaProducerConfig config;

    @BeforeEach
    void setUp() {
        config = new KafkaProducerConfig();

        ReflectionTestUtils.setField(
                config, "bootstrapServers", "localhost:9092"
        );
    }

    @Test
    void producerFactory_ShouldHaveCorrectConfigs() {
        ProducerFactory<String, Object> producerFactory = config.producerFactory();

        assertThat(producerFactory).isNotNull();

        Map<String, Object> configs = producerFactory.getConfigurationProperties();

        assertThat(configs).containsKey(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        assertThat(configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(configs).containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
        assertThat(configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG))
                .isEqualTo(StringSerializer.class);
        assertThat(configs).containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
        assertThat(configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG))
                .isEqualTo(JsonSerializer.class);
    }

    @Test
    void kafkaTemplate_ShouldBeCreated() {
        KafkaTemplate<String, Object> kafkaTemplate = config.kafkaTemplate();

        assertThat(kafkaTemplate).isNotNull();
        assertThat(kafkaTemplate.getProducerFactory()).isNotNull();
    }

    @Test
    void taskNotificationTopic_ShouldBeConfiguredCorrectly() {
        NewTopic topic = config.taskNotificationTopic();

        assertThat(topic).isNotNull();
        assertThat(topic.name()).isEqualTo("task-notifications");
        assertThat(topic.numPartitions()).isEqualTo(1);
        assertThat(topic.replicationFactor()).isEqualTo(((short) 1));
    }
}
