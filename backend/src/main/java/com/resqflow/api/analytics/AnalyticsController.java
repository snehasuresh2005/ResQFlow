package com.resqflow.api.analytics;
import com.resqflow.domain.audit.AuditLog;
import com.resqflow.infrastructure.persistence.AuditLogRepository;
import com.resqflow.infrastructure.persistence.EmergencyRequestRepository;
import com.resqflow.infrastructure.persistence.MissionRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.resqflow.infrastructure.persistence.AllocationRepository;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmergencyRequestRepository requestRepository;
    private final MissionRepository missionRepository;
    private final AuditLogRepository auditLogRepository;
    private final AllocationRepository allocationRepository;

    public AnalyticsController(RedisTemplate<String, Object> redisTemplate,
                               EmergencyRequestRepository requestRepository,
                               MissionRepository missionRepository,
                               AuditLogRepository auditLogRepository,
                               AllocationRepository allocationRepository) {
        this.redisTemplate = redisTemplate;
        this.requestRepository = requestRepository;
        this.missionRepository = missionRepository;
        this.auditLogRepository = auditLogRepository;
        this.allocationRepository = allocationRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardMetricsDto> getDashboardMetrics() {
        long totalReqs = requestRepository.count();
        long criticalReqs = requestRepository.countByPriorityIgnoreCase("CRITICAL");
        long activeMissions = missionRepository.countByStatusIgnoreCaseNotIn(
                List.of("DELIVERED", "FAILED", "CANCELLED"));
        long vehiclesInTransit = missionRepository.countByStatusIgnoreCase("IN_TRANSIT");

        // Calculate fulfillment rate
        long fullyAllocated = requestRepository.countByStatusIgnoreCaseIn(
                List.of("FULLY_ALLOCATED", "FULFILLED"));
        double rate = totalReqs == 0 ? 0.0 : (double) fullyAllocated / totalReqs;

        // Calculate dynamic average response time (minutes) via DB aggregate
        double avgResponseTime = allocationRepository.findAverageResponseTimeMinutes();

        // Calculate dynamic category allocation distribution via DB aggregate
        java.util.Map<String, Double> categoryAllocations = new java.util.HashMap<>();
        categoryAllocations.put("FOOD", 0.0);
        categoryAllocations.put("WATER", 0.0);
        categoryAllocations.put("MEDICAL", 0.0);
        categoryAllocations.put("SHELTER", 0.0);

        for (Object[] row : allocationRepository.findAllocationSumsByCategory()) {
            String type = (String) row[0];
            Double sum = ((Number) row[1]).doubleValue();
            if (categoryAllocations.containsKey(type)) {
                categoryAllocations.put(type, sum);
            }
        }

        DashboardMetricsDto metrics = DashboardMetricsDto.builder()
                .totalRequests(totalReqs)
                .criticalRequests(criticalReqs)
                .activeMissions(activeMissions)
                .vehiclesInTransit(vehiclesInTransit)
                .fulfillmentRate(rate)
                .avgResponseTimeMinutes(avgResponseTime)
                .resourceAllocationByCategory(categoryAllocations)
                .build();

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/audits")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByTimestampDesc().stream().limit(50).collect(Collectors.toList()));
    }
}
