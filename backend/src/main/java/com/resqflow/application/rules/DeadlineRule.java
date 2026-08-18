package com.resqflow.application.rules;

import com.resqflow.domain.request.EmergencyRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class DeadlineRule implements Rule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        EmergencyRequest request = context.getRequest();
        Double travelTimeMinutes = context.getEstimatedTravelTimeMinutes();

        if (request == null) {
            return RuleResult.builder().passed(true).reason("No request context").severity("INFO").build();
        }

        if (travelTimeMinutes == null) {
            // Skip check if routing estimate not yet computed
            return RuleResult.builder().passed(true).reason("No routing estimate available yet").severity("INFO").build();
        }

        LocalDateTime deadline = request.getDeadline();
        LocalDateTime now = LocalDateTime.now();
        
        long minutesToDeadline = Duration.between(now, deadline).toMinutes();

        if (travelTimeMinutes > minutesToDeadline) {
            return RuleResult.builder()
                    .passed(false)
                    .reason(String.format("Estimated travel time (%.1f mins) exceeds remaining deadline time (%d mins)",
                            travelTimeMinutes, minutesToDeadline))
                    .severity("CRITICAL")
                    .build();
        }

        return RuleResult.builder().passed(true).reason("Estimated delivery falls within deadline").severity("INFO").build();
    }
}
