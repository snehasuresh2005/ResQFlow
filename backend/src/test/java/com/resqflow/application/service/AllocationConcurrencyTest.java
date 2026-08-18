package com.resqflow.application.service;

import com.resqflow.domain.location.Depot;
import com.resqflow.domain.location.EmergencyZone;
import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.request.ResourceRequirement;
import com.resqflow.domain.resource.FoodResource;
import com.resqflow.domain.resource.Resource;
import com.resqflow.infrastructure.persistence.DepotRepository;
import com.resqflow.infrastructure.persistence.EmergencyRequestRepository;
import com.resqflow.infrastructure.persistence.EmergencyZoneRepository;
import com.resqflow.infrastructure.persistence.ResourceRepository;
import com.resqflow.infrastructure.persistence.AllocationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class AllocationConcurrencyTest {

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private EmergencyRequestRepository requestRepository;

    @Autowired
    private DepotRepository depotRepository;

    @Autowired
    private EmergencyZoneRepository zoneRepository;

    @Autowired
    private AllocationRepository allocationRepository;

    @Test
    public void testConcurrentAllocationsAreSafe() throws InterruptedException {
        // 1. Prepare DB Entities (clear seeded data for isolation)
        allocationRepository.deleteAll();
        resourceRepository.deleteAll();
        depotRepository.deleteAll();
        requestRepository.deleteAll();

        Depot depot = depotRepository.save(Depot.builder()
                .name("Test Depot")
                .latitude(12.0)
                .longitude(77.0)
                .capacity(1000.0)
                .build());

        EmergencyZone zone = zoneRepository.save(EmergencyZone.builder()
                .name("Test Zone")
                .latitude(12.1)
                .longitude(77.1)
                .severity("HIGH")
                .populationAffected(100)
                .build());

        // Stock available = 50.0 units
        FoodResource resource = new FoodResource();
        resource.setName("Rice Bags");
        resource.setResourceType("FOOD");
        resource.setQuantity(50.0);
        resource.setUnit("units");
        resource.setDepot(depot);
        resource.setWeightPerUnit(1.0);
        resource.setVolumePerUnit(1.0);
        resource.setStatus("AVAILABLE");
        Resource savedResource = resourceRepository.save(resource);

        // Create 10 different requests requiring 10 units each
        List<EmergencyRequest> requests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            EmergencyRequest req = EmergencyRequest.builder()
                    .requestNumber("CREQ-" + i)
                    .emergencyZone(zone)
                    .requestType("FOOD")
                    .priority("HIGH")
                    .numberOfPeopleAffected(50)
                    .deadline(LocalDateTime.now().plusHours(4))
                    .status("CREATED")
                    .build();

            List<ResourceRequirement> requirements = List.of(
                    ResourceRequirement.builder()
                            .request(req)
                            .resourceType("FOOD")
                            .quantity(10.0)
                            .unit("units")
                            .build()
            );
            req.setRequestedResources(requirements);
            requests.add(requestRepository.save(req));
        }

        // 2. Concurrency Test Execution
        // Run 50 parallel threads attempting to allocate
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i % 10; // Distribute requests across threads
            futures.add(executor.submit(() -> {
                latch.await(); // wait for starter trigger
                try {
                    Long reqId = requests.get(index).getId();
                    allocationService.allocateResources(reqId, "NEAREST");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
                return null;
            }));
        }

        latch.countDown(); // trigger execution start simultaneously
        
        for (Future<Void> future : futures) {
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                // ignore thread cancellations/timeouts
            }
        }
        
        executor.shutdown();

        // 3. Verifications
        Resource updatedResource = resourceRepository.findById(savedResource.getId()).orElseThrow();
        
        // Quantity must never be negative
        assertTrue(updatedResource.getQuantity() >= 0.0, "Resource quantity became negative: " + updatedResource.getQuantity());

        // Max possible allocations is 5 (since 5 * 10 = 50 units stock)
        assertTrue(successCount.get() <= 5, "More allocations succeeded than available stock: " + successCount.get());

        System.out.println("Concurrency test summary -> Success: " + successCount.get() + ", Failure: " + failureCount.get() + ", Final Quantity: " + updatedResource.getQuantity());
    }
}
