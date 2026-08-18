package com.resqflow.config;

import com.resqflow.infrastructure.messaging.DomainEventPublisher;
import com.resqflow.infrastructure.messaging.KafkaEventPublisher;
import com.resqflow.infrastructure.messaging.LoggingEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "kafka.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
    }
)
@ActiveProfiles("test")
public class CloudProfileTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testApplicationStartsAndBeansLoadedCorrectly() {
        // 1. Verify application starts with KAFKA_ENABLED=false
        // 2. Verify LoggingEventPublisher is loaded instead of KafkaEventPublisher
        DomainEventPublisher publisher = context.getBean(DomainEventPublisher.class);
        assertNotNull(publisher);
        assertTrue(publisher instanceof LoggingEventPublisher);

        // 3. Verify KafkaEventPublisher is NOT loaded
        assertThrows(NoSuchBeanDefinitionException.class, () -> {
            context.getBean(KafkaEventPublisher.class);
        });

        // 4. Verify PostgreSQL configuration/datasource works
        assertNotNull(dataSource);

        // 5. Verify Redis Template configuration works
        assertNotNull(redisTemplate);

        // 6. Verify health endpoint works
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().contains("UP"));

        // 7. Verify CORS configuration works
        CorsConfigurationSource source = context.getBean("corsConfigurationSource", CorsConfigurationSource.class);
        assertNotNull(source);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod("POST");
        mockRequest.addHeader("Origin", "http://localhost:3000");
        mockRequest.setRequestURI("/api/v1/auth/login");
        
        CorsConfiguration config = source.getCorsConfiguration(mockRequest);
        assertNotNull(config);
        assertTrue(config.getAllowedOrigins().contains("http://localhost:3000"));
    }
}
