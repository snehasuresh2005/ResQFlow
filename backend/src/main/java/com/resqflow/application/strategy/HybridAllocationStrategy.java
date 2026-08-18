package com.resqflow.application.strategy;

import com.resqflow.common.utils.DistanceUtils;
import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.resource.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component("HYBRID")
public class HybridAllocationStrategy implements AllocationStrategy {

    @Override
    public AllocationResult allocate(EmergencyRequest request, String resourceType, Double quantityNeeded,
                                     List<Resource> resources, AllocationContext context) {

        List<Resource> candidates = resources.stream()
                .filter(r -> r.getResourceType().equalsIgnoreCase(resourceType))
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .filter(r -> !r.isExpired())
                .filter(r -> r.getQuantity() > 0)
                .toList();

        double reqLat = request.getEmergencyZone().getLatitude();
        double reqLon = request.getEmergencyZone().getLongitude();

        // Calculate scores and wrap in temporary container
        class CandidateScore {
            final Resource resource;
            final double score;

            CandidateScore(Resource resource, double score) {
                this.resource = resource;
                this.score = score;
            }
        }

        List<CandidateScore> scoredCandidates = new ArrayList<>();

        for (Resource res : candidates) {
            // 1. Urgency Score
            double urgencyScore = switch (request.getPriority().toUpperCase()) {
                case "CRITICAL" -> 1.0;
                case "HIGH" -> 0.8;
                case "MEDIUM" -> 0.5;
                default -> 0.2;
            };

            // 2. Proximity Score (Decays as distance increases, e.g., 50km half-life)
            double distance = DistanceUtils.calculateDistance(reqLat, reqLon, res.getDepot().getLatitude(), res.getDepot().getLongitude());
            double proximityScore = 50.0 / (50.0 + distance);

            // 3. Expiry Score (Urgent expiry gets HIGHER score to avoid wastage)
            double expiryScore = 0.1;
            if (res.getExpiryDate() != null) {
                long daysToExpiry = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), res.getExpiryDate());
                if (daysToExpiry <= 7) {
                    expiryScore = 1.0;
                } else if (daysToExpiry <= 30) {
                    expiryScore = 0.7;
                } else if (daysToExpiry <= 90) {
                    expiryScore = 0.4;
                } else {
                    expiryScore = 0.2;
                }
            }

            // 4. Quantity Score (Prefers single matching batches over fragmenting)
            double quantityScore = res.getQuantity() >= quantityNeeded ? 1.0 : (res.getQuantity() / quantityNeeded);

            // 5. Deadline Score (Closer deadlines get higher scores)
            double deadlineScore = 0.2;
            long hoursToDeadline = Duration.between(LocalDateTime.now(), request.getDeadline()).toHours();
            if (hoursToDeadline <= 3) {
                deadlineScore = 1.0;
            } else if (hoursToDeadline <= 12) {
                deadlineScore = 0.7;
            } else if (hoursToDeadline <= 24) {
                deadlineScore = 0.4;
            }

            // Compute composite weighted score
            double compositeScore = (context.getUrgencyWeight() * urgencyScore)
                    + (context.getProximityWeight() * proximityScore)
                    + (context.getExpiryWeight() * expiryScore)
                    + (context.getQuantityWeight() * quantityScore)
                    + (context.getDeadlineWeight() * deadlineScore);

            scoredCandidates.add(new CandidateScore(res, compositeScore));
        }

        // Sort candidates by score descending
        List<Resource> sortedCandidates = scoredCandidates.stream()
                .sorted(Comparator.comparingDouble((CandidateScore cs) -> cs.score).reversed())
                .map(cs -> cs.resource)
                .toList();

        List<AllocationProposal> proposals = new ArrayList<>();
        double remaining = quantityNeeded;

        for (Resource res : sortedCandidates) {
            if (remaining <= 0) break;

            double take = Math.min(res.getQuantity(), remaining);
            double scoreVal = scoredCandidates.stream().filter(cs -> cs.resource.equals(res)).findFirst().get().score;
            proposals.add(AllocationProposal.builder()
                    .resource(res)
                    .quantity(take)
                    .score(scoreVal)
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
