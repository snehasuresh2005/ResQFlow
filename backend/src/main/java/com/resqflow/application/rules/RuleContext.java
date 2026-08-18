package com.resqflow.application.rules;

import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.resource.Resource;
import com.resqflow.domain.vehicle.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleContext {
    private EmergencyRequest request;
    private Resource resource;
    private Vehicle vehicle;
    private Double quantityNeeded;
    private Double estimatedTravelTimeMinutes; // in minutes
}
