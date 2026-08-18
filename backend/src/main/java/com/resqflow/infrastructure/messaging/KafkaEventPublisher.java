package com.resqflow.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaEventPublisher implements DomainEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private static final String TOPIC = "resqflow-events";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        logger.info("KafkaEventPublisher initialized (KAFKA_ENABLED=true)");
    }

    @Override
    public void publish(String aggregateId, String payload) {
        kafkaTemplate.send(TOPIC, aggregateId, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.error("Failed to publish outbox event to Kafka with aggregate ID: {}", aggregateId, ex);
                    } else {
                        logger.debug("Successfully published outbox event to Kafka to partition: {}", 
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
