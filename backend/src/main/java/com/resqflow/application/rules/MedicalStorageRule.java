package com.resqflow.application.rules;

import com.resqflow.domain.resource.MedicalResource;
import com.resqflow.domain.resource.Resource;
import com.resqflow.domain.vehicle.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class MedicalStorageRule implements Rule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        Resource resource = context.getResource();
        Vehicle vehicle = context.getVehicle();

        if (resource instanceof MedicalResource medical) {
            // If the medical resource needs refrigeration (e.g., temp constraint <= 8.0 degrees C)
            if (medical.getTempConstraint() != null && medical.getTempConstraint() <= 8.0) {
                if (vehicle == null) {
                    return RuleResult.builder()
                            .passed(true)
                            .reason("Medical resource requires refrigeration; matching vehicle will be required.")
                            .severity("WARNING")
                            .build();
                }

                if (!"AMBULANCE".equalsIgnoreCase(vehicle.getVehicleType())) {
                    return RuleResult.builder()
                            .passed(false)
                            .reason("Medical resource requires cold chain storage, which is only supported by AMBULANCE vehicles.")
                            .severity("CRITICAL")
                            .build();
                }
            }
        }

        return RuleResult.builder().passed(true).reason("Medical storage check passed").severity("INFO").build();
    }
}
