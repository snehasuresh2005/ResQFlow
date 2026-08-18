package com.resqflow.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resqflow.domain.audit.AuditLog;
import com.resqflow.infrastructure.persistence.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuditLogConsumer {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogConsumer.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogConsumer(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "resqflow-events", groupId = "resqflow-audit-group")
    public void consume(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            
            // Extract some info for auditing
            String requestNumber = jsonNode.has("requestNumber") ? jsonNode.get("requestNumber").asText() : "N/A";
            String status = jsonNode.has("status") ? jsonNode.get("status").asText() : "";
            
            String action = "SYSTEM_EVENT";
            String details = message;
            
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
            logger.error("Failed to process event for audit logging", e);
        }
    }
}
