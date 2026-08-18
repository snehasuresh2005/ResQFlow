package com.resqflow.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DashboardUpdateConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DashboardUpdateConsumer.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public DashboardUpdateConsumer(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "resqflow-events", groupId = "resqflow-dashboard-group")
    public void consume(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            // Check if it's a request created event
            if (jsonNode.has("requestNumber") && !jsonNode.has("allocations") && !jsonNode.has("vehicle")) {
                redisTemplate.opsForValue().increment("metrics:total_requests");
                String priority = jsonNode.has("priority") ? jsonNode.get("priority").asText() : "";
                if ("CRITICAL".equalsIgnoreCase(priority)) {
                    redisTemplate.opsForValue().increment("metrics:critical_requests");
                }
                logger.debug("Metrics updated for request creation");
            }
            
            // Check if it's a mission event
            if (jsonNode.has("route") && jsonNode.has("status")) {
                String status = jsonNode.get("status").asText();
                if ("CREATED".equalsIgnoreCase(status)) {
                    redisTemplate.opsForValue().increment("metrics:active_missions");
                } else if ("IN_TRANSIT".equalsIgnoreCase(status)) {
                    redisTemplate.opsForValue().increment("metrics:vehicles_in_transit");
                } else if ("DELIVERED".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
                    redisTemplate.opsForValue().decrement("metrics:active_missions");
                    // Ensure count doesn't drop below 0
                    Object inTransitVal = redisTemplate.opsForValue().get("metrics:vehicles_in_transit");
                    if (inTransitVal != null && Integer.parseInt(inTransitVal.toString()) > 0) {
                        redisTemplate.opsForValue().decrement("metrics:vehicles_in_transit");
                    }
                }
                logger.debug("Metrics updated for mission state transition to: {}", status);
            }

        } catch (Exception e) {
            logger.error("Failed to parse event in DashboardUpdateConsumer", e);
        }
    }
}
