package com.resqflow.api.analytics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardMetricsDto {
    private long totalRequests;
    private long criticalRequests;
    private long activeMissions;
    private long vehiclesInTransit;
    private double fulfillmentRate;
    private double avgResponseTimeMinutes;
    private java.util.Map<String, Double> resourceAllocationByCategory;
}
