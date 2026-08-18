package com.resqflow.application.strategy;

import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.resource.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("FAIR_DISTRIBUTION")
public class FairDistributionStrategy implements AllocationStrategy {

    @Override
    public AllocationResult allocate(EmergencyRequest request, String resourceType, Double quantityNeeded,
                                     List<Resource> resources, AllocationContext context) {

        List<Resource> candidates = resources.stream()
                .filter(r -> r.getResourceType().equalsIgnoreCase(resourceType))
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .filter(r -> !r.isExpired())
                .filter(r -> r.getQuantity() > 0)
                .toList();

        double totalAvailable = candidates.stream().mapToDouble(Resource::getQuantity).sum();

        List<AllocationProposal> proposals = new ArrayList<>();
        double remaining = quantityNeeded;

        if (totalAvailable <= 0) {
            return AllocationResult.builder()
                    .proposals(proposals)
                    .fullyAllocated(false)
                    .allocatedQuantity(0.0)
                    .remainingNeeded(quantityNeeded)
                    .build();
        }

        if (totalAvailable <= quantityNeeded) {
            // Take everything
            for (Resource res : candidates) {
                proposals.add(AllocationProposal.builder()
                        .resource(res)
                        .quantity(res.getQuantity())
                        .score(1.0)
                        .build());
            }
            return AllocationResult.builder()
                    .proposals(proposals)
                    .fullyAllocated(totalAvailable == quantityNeeded)
                    .allocatedQuantity(totalAvailable)
                    .remainingNeeded(Math.max(0.0, quantityNeeded - totalAvailable))
                    .build();
        }

        // Proportional allocation: take ratio from each candidate
        double ratio = quantityNeeded / totalAvailable;
        for (Resource res : candidates) {
            double take = Math.floor(res.getQuantity() * ratio * 100) / 100; // avoid floating point issues
            if (take > 0) {
                proposals.add(AllocationProposal.builder()
                        .resource(res)
                        .quantity(take)
                        .score(ratio)
                        .build());
                remaining -= take;
            }
        }

        // Top up any tiny remainder left due to flooring rounding
        if (remaining > 0.01) {
            for (AllocationProposal prop : proposals) {
                if (remaining <= 0) break;
                double resLeft = prop.getResource().getQuantity() - prop.getQuantity();
                double extra = Math.min(resLeft, remaining);
                prop.setQuantity(prop.getQuantity() + extra);
                remaining -= extra;
            }
        }

        double allocated = quantityNeeded - remaining;
        return AllocationResult.builder()
                .proposals(proposals)
                .fullyAllocated(remaining <= 0.05)
                .allocatedQuantity(allocated)
                .remainingNeeded(remaining)
                .build();
    }
}
