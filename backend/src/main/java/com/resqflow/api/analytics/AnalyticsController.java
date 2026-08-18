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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmergencyRequestRepository requestRepository;
    private final MissionRepository missionRepository;
    private final AuditLogRepository auditLogRepository;

    public AnalyticsController(RedisTemplate<String, Object> redisTemplate,
                               EmergencyRequestRepository requestRepository,
                               MissionRepository missionRepository,
                               AuditLogRepository auditLogRepository) {
        this.redisTemplate = redisTemplate;
        this.requestRepository = requestRepository;
        this.missionRepository = missionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardMetricsDto> getDashboardMetrics() {
        long totalReqs = getMetric("metrics:total_requests", () -> requestRepository.count());
        long criticalReqs = getMetric("metrics:critical_requests", () -> 
                requestRepository.findAll().stream().filter(r -> "CRITICAL".equalsIgnoreCase(r.getPriority())).count());
        long activeMissions = getMetric("metrics:active_missions", () -> 
                missionRepository.findAll().stream().filter(m -> !"DELIVERED".equalsIgnoreCase(m.getStatus()) && !"FAILED".equalsIgnoreCase(m.getStatus()) && !"CANCELLED".equalsIgnoreCase(m.getStatus())).count());
        long vehiclesInTransit = getMetric("metrics:vehicles_in_transit", () -> 
                missionRepository.findByStatus("IN_TRANSIT").size());

        // Calculate fulfillment rate
        long total = requestRepository.count();
        long fullyAllocated = requestRepository.findByStatus("FULLY_ALLOCATED").size() + requestRepository.findByStatus("FULFILLED").size();
        double rate = total == 0 ? 0.0 : (double) fullyAllocated / total;

        DashboardMetricsDto metrics = DashboardMetricsDto.builder()
                .totalRequests(totalReqs)
                .criticalRequests(criticalReqs)
                .activeMissions(activeMissions)
                .vehiclesInTransit(vehiclesInTransit)
                .fulfillmentRate(rate)
                .avgResponseTimeMinutes(18.3) // static simulation default or calculated
                .build();

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/audits")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByTimestampDesc().stream().limit(50).collect(Collectors.toList()));
    }

    private long getMetric(String cacheKey, java.util.function.LongSupplier databaseFallback) {
        Object cachedVal = redisTemplate.opsForValue().get(cacheKey);
        if (cachedVal != null) {
            try {
                return Long.parseLong(cachedVal.toString());
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        
        long dbVal = databaseFallback.getAsLong();
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(dbVal));
        return dbVal;
    }
}
