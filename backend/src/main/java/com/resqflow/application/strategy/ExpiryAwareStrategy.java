package com.resqflow.application.strategy;

import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.resource.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component("EXPIRY_AWARE")
public class ExpiryAwareStrategy implements AllocationStrategy {

    @Override
    public AllocationResult allocate(EmergencyRequest request, String resourceType, Double quantityNeeded,
                                     List<Resource> resources, AllocationContext context) {

        // Filter: matching resource type, available, not expired
        // Sort: expiry date ascending (nulls last)
        List<Resource> candidates = resources.stream()
                .filter(r -> r.getResourceType().equalsIgnoreCase(resourceType))
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .filter(r -> !r.isExpired())
                .filter(r -> r.getQuantity() > 0)
                .sorted(Comparator.comparing(Resource::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<AllocationProposal> proposals = new ArrayList<>();
        double remaining = quantityNeeded;

        for (Resource res : candidates) {
            if (remaining <= 0) break;

            double take = Math.min(res.getQuantity(), remaining);
            proposals.add(AllocationProposal.builder()
                    .resource(res)
                    .quantity(take)
                    .score(res.getExpiryDate() != null ? (double) res.getExpiryDate().toEpochDay() : Double.MAX_VALUE)
                    .build());
            remaining -= take;
        }

        double allocated = quantityNeeded - remaining;
        return AllocationResult.builder()
                .proposals(proposals)
                .fullyAllocated(remaining <= 0)
                .allocatedQuantity(allocated)
                .remainingNeeded(remaining)
                .build();
    }
}
