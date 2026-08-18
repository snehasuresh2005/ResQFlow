package com.resqflow.infrastructure.messaging;

import com.resqflow.domain.event.OutboxEvent;
import com.resqflow.infrastructure.persistence.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutboxPublisherService {
    private static final Logger logger = LoggerFactory.getLogger(OutboxPublisherService.class);

    private static final String TOPIC = "resqflow-events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisherService(OutboxEventRepository outboxEventRepository,
                                  KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
        if (pendingEvents.isEmpty()) {
            return;
        }

        logger.debug("Found {} pending outbox events to publish to Kafka.", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // Key the Kafka record by aggregate ID to maintain order of operations per entity
                kafkaTemplate.send(TOPIC, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.error("Failed to publish outbox event: {}", event.getId(), ex);
                            } else {
                                logger.debug("Successfully published outbox event: {} to partition: {}", 
                                        event.getId(), result.getRecordMetadata().partition());
                            }
                        });

                event.setProcessed(true);
                outboxEventRepository.save(event);
            } catch (Exception e) {
                logger.error("Error processing outbox event ID: {}", event.getId(), e);
            }
        }
    }
}
