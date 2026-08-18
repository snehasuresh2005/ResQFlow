package com.resqflow.infrastructure.messaging;

public interface DomainEventPublisher {
    void publish(String aggregateId, String payload);
}
