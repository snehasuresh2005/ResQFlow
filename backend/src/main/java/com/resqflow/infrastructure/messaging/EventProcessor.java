package com.resqflow.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resqflow.domain.audit.AuditLog;
import com.resqflow.infrastructure.persistence.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    private final AuditLogRepository auditLogRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public EventProcessor(AuditLogRepository auditLogRepository,
                          RedisTemplate<String, Object> redisTemplate,
                          ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void processEvent(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            // 1. Process Audit Log
            processAuditLog(jsonNode, message);

            // 2. Process Dashboard metrics in Redis
            processDashboardMetrics(jsonNode);

        } catch (Exception e) {
            logger.error("Failed to process event message: {}", message, e);
        }
    }

    private void processAuditLog(JsonNode jsonNode, String rawMessage) {
        try {
            String requestNumber = jsonNode.has("requestNumber") ? jsonNode.get("requestNumber").asText() : "N/A";
            String status = jsonNode.has("status") ? jsonNode.get("status").asText() : "";
            
            String action = "SYSTEM_EVENT";
            String details = rawMessage;
            
            if (jsonNode.has("allocations")) {
                action = "RESOURCE_ALLOCATION";
                details = "Allocated resources for request: " + requestNumber;
            } else if (jsonNode.has("vehicleType")) {
                action = "VEHICLE_REGISTRATION";
                details = "Registered vehicle: " + jsonNode.get("registrationNumber").asText();
            } else if (jsonNode.has("routeNodes")) {
                action = "MISSION_UPDATE";
                details = "Mission updated with status: " + status;
            }

            AuditLog log = AuditLog.builder()
                    .action(action)
                    .details(details)
                    .build();

            auditLogRepository.save(log);
            logger.info("Saved audit log for action: {}", action);
        } catch (Exception e) {
            logger.error("Error writing audit log", e);
        }
    }

    private void processDashboardMetrics(JsonNode jsonNode) {
        try {
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
            logger.error("Error updating dashboard metrics in Redis", e);
        }
    }
}
