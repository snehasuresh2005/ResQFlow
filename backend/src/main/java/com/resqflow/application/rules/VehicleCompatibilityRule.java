package com.resqflow.application.rules;

import com.resqflow.domain.vehicle.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleCompatibilityRule implements Rule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        Vehicle vehicle = context.getVehicle();
        if (vehicle == null) {
            return RuleResult.builder().passed(true).reason("No vehicle specified yet").severity("INFO").build();
        }

        // Default to LAND route check. Boat requires WATER.
        String requiredRouteType = "LAND";
        if ("BOAT".equalsIgnoreCase(vehicle.getVehicleType())) {
            requiredRouteType = "WATER";
        }

        if (!vehicle.supportsRouteType(requiredRouteType)) {
            return RuleResult.builder()
                    .passed(false)
                    .reason("Vehicle type " + vehicle.getVehicleType() + " is incompatible with route type " + requiredRouteType)
                    .severity("CRITICAL")
                    .build();
        }

        return RuleResult.builder().passed(true).reason("Vehicle matches route requirements").severity("INFO").build();
    }
}
