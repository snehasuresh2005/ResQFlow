package com.resqflow.application.strategy;

import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.resource.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component("HIGHEST_PRIORITY")
public class HighestPriorityStrategy implements AllocationStrategy {

    @Override
    public AllocationResult allocate(EmergencyRequest request, String resourceType, Double quantityNeeded,
                                     List<Resource> resources, AllocationContext context) {

        // Filter: matching resource type, available, not expired
        // Sort: priority descending
        List<Resource> candidates = resources.stream()
                .filter(r -> r.getResourceType().equalsIgnoreCase(resourceType))
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .filter(r -> !r.isExpired())
                .filter(r -> r.getQuantity() > 0)
                .sorted(Comparator.comparingInt(Resource::getPriority).reversed())
                .toList();

        List<AllocationProposal> proposals = new ArrayList<>();
        double remaining = quantityNeeded;

        for (Resource res : candidates) {
            if (remaining <= 0) break;

            double take = Math.min(res.getQuantity(), remaining);
            proposals.add(AllocationProposal.builder()
                    .resource(res)
                    .quantity(take)
                    .score((double) res.getPriority())
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
