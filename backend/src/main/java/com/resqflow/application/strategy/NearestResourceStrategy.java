package com.resqflow.application.strategy;

import com.resqflow.common.utils.DistanceUtils;
import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.resource.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component("NEAREST")
public class NearestResourceStrategy implements AllocationStrategy {

    @Override
    public AllocationResult allocate(EmergencyRequest request, String resourceType, Double quantityNeeded,
                                     List<Resource> resources, AllocationContext context) {
        
        double reqLat = request.getEmergencyZone().getLatitude();
        double reqLon = request.getEmergencyZone().getLongitude();

        // Filter: matching resource type, available, not expired
        List<Resource> candidates = resources.stream()
                .filter(r -> r.getResourceType().equalsIgnoreCase(resourceType))
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .filter(r -> !r.isExpired())
                .filter(r -> r.getQuantity() > 0)
                .sorted(Comparator.comparingDouble(r -> 
                    DistanceUtils.calculateDistance(reqLat, reqLon, r.getDepot().getLatitude(), r.getDepot().getLongitude())))
                .toList();

        List<AllocationProposal> proposals = new ArrayList<>();
        double remaining = quantityNeeded;

        for (Resource res : candidates) {
            if (remaining <= 0) break;
            
            double take = Math.min(res.getQuantity(), remaining);
            proposals.add(AllocationProposal.builder()
                    .resource(res)
                    .quantity(take)
                    .score(DistanceUtils.calculateDistance(reqLat, reqLon, res.getDepot().getLatitude(), res.getDepot().getLongitude()))
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
