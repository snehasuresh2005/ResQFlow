package com.resqflow.infrastructure.messaging;

import com.resqflow.domain.event.OutboxEvent;
import com.resqflow.infrastructure.persistence.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutboxPublisherService {
    private static final Logger logger = LoggerFactory.getLogger(OutboxPublisherService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final DomainEventPublisher domainEventPublisher;

    public OutboxPublisherService(OutboxEventRepository outboxEventRepository,
                                  DomainEventPublisher domainEventPublisher) {
        this.outboxEventRepository = outboxEventRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
        if (pendingEvents.isEmpty()) {
            return;
        }

        logger.debug("Found {} pending outbox events to publish.", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                domainEventPublisher.publish(event.getAggregateId(), event.getPayload());

                event.setProcessed(true);
                outboxEventRepository.save(event);
            } catch (Exception e) {
                logger.error("Error processing outbox event ID: {}", event.getId(), e);
            }
        }
    }
}
