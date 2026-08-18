package com.resqflow.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "false")
public class LoggingEventPublisher implements DomainEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(LoggingEventPublisher.class);

    private final EventProcessor eventProcessor;

    public LoggingEventPublisher(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
        logger.info("LoggingEventPublisher initialized (KAFKA_ENABLED=false)");
    }

    @Override
    public void publish(String aggregateId, String payload) {
        logger.info("[KAFKA DISABLED - LOCAL DISPATCH] Event: key={}, payload={}", aggregateId, payload);
        // Directly process the event locally since there is no Kafka broker
        eventProcessor.processEvent(payload);
    }
}
