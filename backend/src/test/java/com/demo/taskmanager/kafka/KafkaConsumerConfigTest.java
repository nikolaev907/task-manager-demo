package com.demo.taskmanager.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerConfigTest {

    private KafkaConsumerConfig kafkaConsumerConfig;

    @BeforeEach
    void setUp() {
        kafkaConsumerConfig = new KafkaConsumerConfig();

        ReflectionTestUtils.setField(kafkaConsumerConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConsumerConfig, "groupId", "task-group");
        ReflectionTestUtils.setField(kafkaConsumerConfig, "enableAutoCommit", "true");
    }

    @Test
    void consumerFactory_ShouldBeConfiguredCorrectly() {
        ConsumerFactory<String, Object> consumerFactory = kafkaConsumerConfig.consumerFactory();

        assertThat(consumerFactory).isNotNull();

        Map<String, Object> configs = consumerFactory.getConfigurationProperties();

        assertThat(configs).containsKey(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
        assertThat(configs.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");

        assertThat(configs).containsKey(ConsumerConfig.GROUP_ID_CONFIG);
        assertThat(configs.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo("task-group");

        assertThat(configs).containsKey(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG);
        assertThat(configs.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)).isEqualTo("true");

        assertThat(configs).containsKey(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
        assertThat(configs.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG))
                .isEqualTo(StringDeserializer.class);

        assertThat(configs).containsKey(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
        assertThat(configs.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG))
                .isEqualTo(JsonDeserializer.class);
    }

    @Test
    void consumerFactory_ShouldHaveTypeMappings() {
        ConsumerFactory<String, Object> consumerFactory = kafkaConsumerConfig.consumerFactory();

        Map<String, Object> configs = consumerFactory.getConfigurationProperties();

        assertThat(configs).containsKey(JsonDeserializer.TYPE_MAPPINGS);
        String typeMappings = (String) configs.get(JsonDeserializer.TYPE_MAPPINGS);
        assertThat(typeMappings).contains("notification");
        assertThat(typeMappings).contains("com.demo.taskmanager.model.Notification");
    }

    @Test
    void kafkaListenerContainerFactory_ShouldBeConfigured() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                kafkaConsumerConfig.kafkaListenerContainerFactory();

        assertThat(factory).isNotNull();
        assertThat(factory.getConsumerFactory()).isNotNull();
        assertThat(factory.getContainerProperties()).isNotNull();
    }

    @Test
    void consumerFactory_ShouldHandleDifferentConfigValues() {
        ReflectionTestUtils.setField(kafkaConsumerConfig, "enableAutoCommit", "false");

        ConsumerFactory<String, Object> consumerFactory = kafkaConsumerConfig.consumerFactory();
        Map<String, Object> configs = consumerFactory.getConfigurationProperties();

        assertThat(configs.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)).isEqualTo("false");
    }
}
