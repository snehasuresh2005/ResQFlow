package com.resqflow.application.rules;

import com.resqflow.domain.resource.Resource;
import com.resqflow.domain.vehicle.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class CapacityRule implements Rule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        Vehicle vehicle = context.getVehicle();
        Resource resource = context.getResource();
        Double quantity = context.getQuantityNeeded();

        if (vehicle == null) {
            // If no vehicle is selected yet, skip capacity check
            return RuleResult.builder().passed(true).reason("No vehicle specified for allocation yet").severity("INFO").build();
        }

        if (resource == null || quantity == null) {
            return RuleResult.builder().passed(false).reason("Missing resource or quantity details").severity("CRITICAL").build();
        }

        double totalWeight = resource.getWeightPerUnit() * quantity;
        double totalVolume = resource.getVolumePerUnit() * quantity;

        if (!vehicle.canCarry(totalWeight, totalVolume)) {
            return RuleResult.builder()
                    .passed(false)
                    .reason(String.format("Vehicle capacity exceeded. Needed: Weight %.2fkg, Volume %.2fm3. Available: Weight %.2fkg, Volume %.2fm3.",
                            totalWeight, totalVolume, vehicle.getCapacityWeight(), vehicle.getCapacityVolume()))
                    .severity("CRITICAL")
                    .build();
        }

        return RuleResult.builder().passed(true).reason("Vehicle has sufficient capacity").severity("INFO").build();
    }
}
