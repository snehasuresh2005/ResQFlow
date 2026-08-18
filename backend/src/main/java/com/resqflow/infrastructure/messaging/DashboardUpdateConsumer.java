package com.resqflow.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class DashboardUpdateConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DashboardUpdateConsumer.class);

    private final EventProcessor eventProcessor;

    public DashboardUpdateConsumer(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    @KafkaListener(topics = "resqflow-events", groupId = "resqflow-dashboard-group")
    public void consume(String message) {
        logger.debug("DashboardUpdateConsumer received event from Kafka: {}", message);
        eventProcessor.processEvent(message);
    }
}
