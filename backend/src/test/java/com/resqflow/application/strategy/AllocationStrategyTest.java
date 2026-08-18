package com.resqflow.application.strategy;

import com.resqflow.domain.location.Depot;
import com.resqflow.domain.location.EmergencyZone;
import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.resource.FoodResource;
import com.resqflow.domain.resource.Resource;
import com.resqflow.domain.resource.WaterResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AllocationStrategyTest {

    private EmergencyRequest request;
    private Depot depotNear;
    private Depot depotFar;
    private Resource resourceNear;
    private Resource resourceFar;
    private AllocationContext context;

    @BeforeEach
    public void setUp() {
        context = new AllocationContext();

        // Zone at lat 12.0, lon 77.0
        EmergencyZone zone = EmergencyZone.builder()
                .latitude(12.0)
                .longitude(77.0)
                .severity("CRITICAL")
                .populationAffected(100)
                .build();

        request = EmergencyRequest.builder()
                .emergencyZone(zone)
                .priority("CRITICAL")
                .deadline(java.time.LocalDateTime.now().plusHours(4))
                .build();

        // Depot near zone: lat 12.05, lon 77.05
        depotNear = Depot.builder().name("Near Depot").latitude(12.05).longitude(77.05).build();
        
        // Depot far from zone: lat 13.5, lon 78.5
        depotFar = Depot.builder().name("Far Depot").latitude(13.5).longitude(78.5).build();

        // Expiry date far in the future
        resourceNear = new FoodResource();
        resourceNear.setId(1L);
        resourceNear.setName("Near Food Resource");
        resourceNear.setResourceType("FOOD");
        resourceNear.setQuantity(100.0);
        resourceNear.setDepot(depotNear);
        resourceNear.setExpiryDate(LocalDate.now().plusDays(90));
        resourceNear.setWeightPerUnit(1.0);
        resourceNear.setVolumePerUnit(1.0);
        resourceNear.setStatus("AVAILABLE");

        // Expiry date soon
        resourceFar = new FoodResource();
        resourceFar.setId(2L);
        resourceFar.setName("Far Expiring Food Resource");
        resourceFar.setResourceType("FOOD");
        resourceFar.setQuantity(100.0);
        resourceFar.setDepot(depotFar);
        resourceFar.setExpiryDate(LocalDate.now().plusDays(2)); // soon
        resourceFar.setWeightPerUnit(1.0);
        resourceFar.setVolumePerUnit(1.0);
        resourceFar.setStatus("AVAILABLE");
    }

    @Test
    public void testNearestResourceStrategy() {
        NearestResourceStrategy strategy = new NearestResourceStrategy();
        List<Resource> resources = List.of(resourceFar, resourceNear);

        // Request 50 units. Nearest strategy should choose resourceNear because it is closer.
        AllocationResult result = strategy.allocate(request, "FOOD", 50.0, resources, context);

        assertTrue(result.isFullyAllocated());
        assertEquals(50.0, result.getAllocatedQuantity());
        assertEquals(1, result.getProposals().size());
        assertEquals(resourceNear.getId(), result.getProposals().get(0).getResource().getId());
    }

    @Test
    public void testExpiryAwareStrategy() {
        ExpiryAwareStrategy strategy = new ExpiryAwareStrategy();
        List<Resource> resources = List.of(resourceNear, resourceFar);

        // Expiry Aware should select resourceFar because it expires in 2 days (versus 90 days).
        AllocationResult result = strategy.allocate(request, "FOOD", 50.0, resources, context);

        assertTrue(result.isFullyAllocated());
        assertEquals(50.0, result.getAllocatedQuantity());
        assertEquals(1, result.getProposals().size());
        assertEquals(resourceFar.getId(), result.getProposals().get(0).getResource().getId());
    }

    @Test
    public void testHybridAllocationStrategy() {
        HybridAllocationStrategy strategy = new HybridAllocationStrategy();
        List<Resource> resources = List.of(resourceNear, resourceFar);

        // Proximity weight is 0.8, Expiry is 0.1
        context.setProximityWeight(0.8);
        context.setExpiryWeight(0.1);
        context.setUrgencyWeight(0.0);
        context.setQuantityWeight(0.0);
        context.setDeadlineWeight(0.0);

        AllocationResult result = strategy.allocate(request, "FOOD", 50.0, resources, context);
        // Should choose Near due to high proximity weight
        assertEquals(resourceNear.getId(), result.getProposals().get(0).getResource().getId());
    }
}
