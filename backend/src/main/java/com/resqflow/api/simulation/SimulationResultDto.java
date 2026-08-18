package com.resqflow.api.simulation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimulationResultDto {
    private Long simulationId;
    private Integer requestsProcessed;
    private Integer requestsFulfilled;
    private Double criticalFulfillmentRate;
    private Double averageResponseTimeMinutes;
    private Double vehicleUtilization;
    private Double resourceWastage;
}
