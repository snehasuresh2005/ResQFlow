package com.resqflow.config;

import com.resqflow.infrastructure.messaging.DomainEventPublisher;
import com.resqflow.infrastructure.messaging.KafkaEventPublisher;
import com.resqflow.infrastructure.messaging.LoggingEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    properties = {
        "kafka.enabled=true",
        "spring.kafka.bootstrap-servers=localhost:9092"
    }
)
@ActiveProfiles("test")
public class KafkaEnabledProfileTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testKafkaPublisherLoadedWhenEnabled() {
        // 1. Verify DomainEventPublisher is loaded
        DomainEventPublisher publisher = context.getBean(DomainEventPublisher.class);
        assertNotNull(publisher);
        
        // 2. Verify KafkaEventPublisher is loaded when enabled
        assertTrue(publisher instanceof KafkaEventPublisher);

        // 3. Verify LoggingEventPublisher is NOT loaded
        assertThrows(NoSuchBeanDefinitionException.class, () -> {
            context.getBean(LoggingEventPublisher.class);
        });
    }
}
