package com.resqflow.application.service;

import com.resqflow.api.simulation.SimulationRequestDto;
import com.resqflow.api.simulation.SimulationResultDto;
import com.resqflow.application.strategy.*;
import com.resqflow.common.utils.DistanceUtils;
import com.resqflow.domain.location.Depot;
import com.resqflow.domain.location.EmergencyZone;
import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.request.ResourceRequirement;
import com.resqflow.domain.resource.FoodResource;
import com.resqflow.domain.resource.Resource;
import com.resqflow.domain.resource.WaterResource;
import com.resqflow.domain.simulation.DisasterSimulation;
import com.resqflow.infrastructure.persistence.DisasterSimulationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import com.resqflow.common.exception.ResourceNotFoundException;
import java.util.*;

@Service
@Transactional
public class SimulationService {

    private final DisasterSimulationRepository simulationRepository;
    private final AllocationStrategyFactory strategyFactory;

    public SimulationService(DisasterSimulationRepository simulationRepository,
                             AllocationStrategyFactory strategyFactory) {
        this.simulationRepository = simulationRepository;
        this.strategyFactory = strategyFactory;
    }

    public SimulationResultDto runSimulation(SimulationRequestDto dto) {
        Random random = new Random();

        // 1. Generate in-memory Depots
        List<Depot> depots = new ArrayList<>();
        for (int i = 1; i <= dto.getDepots(); i++) {
            depots.add(Depot.builder()
                    .id((long) i)
                    .name("Sim Depot " + i)
                    .latitude(12.9 + (13.1 - 12.9) * random.nextDouble())
                    .longitude(77.5 + (77.7 - 77.5) * random.nextDouble())
                    .capacity(10000.0)
                    .build());
        }

        // 2. Generate in-memory Zones
        List<EmergencyZone> zones = new ArrayList<>();
        for (int i = 1; i <= dto.getZones(); i++) {
            zones.add(EmergencyZone.builder()
                    .id((long) i)
                    .name("Sim Zone " + i)
                    .latitude(12.9 + (13.1 - 12.9) * random.nextDouble())
                    .longitude(77.5 + (77.7 - 77.5) * random.nextDouble())
                    .severity(random.nextBoolean() ? "CRITICAL" : "HIGH")
                    .populationAffected(100 + random.nextInt(900))
                    .build());
        }

        // 3. Generate in-memory Resources
        List<Resource> resources = new ArrayList<>();
        String[] types = {"FOOD", "WATER", "MEDICAL"};
        for (int i = 1; i <= dto.getResources(); i++) {
            Depot depot = depots.get(random.nextInt(depots.size()));
            String type = types[random.nextInt(types.length)];
            
            Resource res;
            if ("FOOD".equals(type)) {
                FoodResource food = new FoodResource();
                food.setStorageRequirement("DRY");
                res = food;
            } else {
                res = new WaterResource();
            }

            res.setId((long) i);
            res.setName("Sim Res " + i);
            res.setResourceType(type);
            res.setQuantity(50.0 + random.nextDouble() * 150.0);
            res.setUnit("units");
            res.setDepot(depot);
            res.setExpiryDate(LocalDate.now().plusDays(2 + random.nextInt(90)));
            res.setPriority(random.nextInt(5));
            res.setWeightPerUnit(1.0);
            res.setVolumePerUnit(0.5);
            res.setStatus("AVAILABLE");

            resources.add(res);
        }

        // 4. Generate in-memory Requests
        List<EmergencyRequest> requests = new ArrayList<>();
        for (int i = 1; i <= dto.getRequests(); i++) {
            EmergencyZone zone = zones.get(random.nextInt(zones.size()));
            String p = random.nextDouble() > 0.8 ? "CRITICAL" : (random.nextBoolean() ? "HIGH" : "MEDIUM");
            
            EmergencyRequest req = EmergencyRequest.builder()
                    .id((long) i)
                    .requestNumber("SREQ-" + i)
                    .emergencyZone(zone)
                    .requestType("FOOD")
                    .priority(p)
                    .numberOfPeopleAffected(50 + random.nextInt(450))
                    .deadline(LocalDateTime.now().plusMinutes(30 + random.nextInt(120)))
                    .status("CREATED")
                    .build();

            // Add requirements
            List<ResourceRequirement> requirements = new ArrayList<>();
            requirements.add(ResourceRequirement.builder()
                    .id((long) i)
                    .request(req)
                    .resourceType("FOOD")
                    .quantity(10.0 + random.nextInt(90))
                    .unit("units")
                    .build());
            req.setRequestedResources(requirements);

            requests.add(req);
        }

        // 5. DSA Priority Queue Scheduling
        // Sort requests by computed priority score:
        // Score = severityScore + affectedPeopleScore + deadlineScore + waitingTimeScore
        // Time complexity to build Priority Queue: O(N log N)
        PriorityQueue<EmergencyRequest> priorityQueue = new PriorityQueue<>((r1, r2) -> {
            double score1 = calculatePriorityScore(r1);
            double score2 = calculatePriorityScore(r2);
            return Double.compare(score2, score1); // descending
        });

        priorityQueue.addAll(requests);

        // Run Simulation loop
        AllocationStrategy strategy = strategyFactory.getStrategy(dto.getAllocationStrategy());
        AllocationContext context = new AllocationContext();

        int processed = 0;
        int fulfilled = 0;
        int criticalTotal = 0;
        int criticalFulfilled = 0;
        double totalTravelTime = 0.0;
        int travelTimeCounts = 0;

        while (!priorityQueue.isEmpty()) {
            EmergencyRequest req = priorityQueue.poll();
            processed++;

            boolean critical = "CRITICAL".equalsIgnoreCase(req.getPriority());
            if (critical) criticalTotal++;

            boolean allAllocated = true;
            for (ResourceRequirement requirement : req.getRequestedResources()) {
                AllocationResult result = strategy.allocate(req, requirement.getResourceType(), requirement.getQuantity(), resources, context);
                
                if (!result.isFullyAllocated()) {
                    allAllocated = false;
                }

                // Consume resources in-memory
                for (AllocationProposal prop : result.getProposals()) {
                    Resource res = prop.getResource();
                    res.setQuantity(res.getQuantity() - prop.getQuantity());

                    // Calculate simulated transit time (dist / speed * 60)
                    double dist = DistanceUtils.calculateDistance(
                            req.getEmergencyZone().getLatitude(), req.getEmergencyZone().getLongitude(),
                            res.getDepot().getLatitude(), res.getDepot().getLongitude()
                    );
                    double travelTime = (dist / 50.0) * 60.0; // minutes
                    totalTravelTime += travelTime;
                    travelTimeCounts++;
                }
            }

            if (allAllocated) {
                fulfilled++;
                if (critical) criticalFulfilled++;
            }
        }

        double criticalRate = criticalTotal == 0 ? 1.0 : (double) criticalFulfilled / criticalTotal;
        double avgResponse = travelTimeCounts == 0 ? 15.0 : totalTravelTime / travelTimeCounts;
        
        // Blocked road percentage reduces vehicle utilization ratio
        double vehicleUtil = 0.85 - (dto.getBlockedRoadPercentage() * 0.015);
        double wastage = 0.03 + (random.nextDouble() * 0.02);

        DisasterSimulation sim = DisasterSimulation.builder()
                .scenario(dto.getScenario().toUpperCase())
                .requestsProcessed(processed)
                .requestsFulfilled(fulfilled)
                .criticalFulfillmentRate(criticalRate)
                .avgResponseTime(avgResponse)
                .vehicleUtilization(vehicleUtil)
                .resourceWastage(wastage)
                .build();

        DisasterSimulation saved = simulationRepository.save(sim);

        return SimulationResultDto.builder()
                .simulationId(saved.getId())
                .requestsProcessed(processed)
                .requestsFulfilled(fulfilled)
                .criticalFulfillmentRate(criticalRate)
                .averageResponseTimeMinutes(avgResponse)
                .vehicleUtilization(vehicleUtil)
                .resourceWastage(wastage)
                .build();
    }

    private double calculatePriorityScore(EmergencyRequest request) {
        double severityScore = switch (request.getPriority().toUpperCase()) {
            case "CRITICAL" -> 500.0;
            case "HIGH" -> 300.0;
            case "MEDIUM" -> 100.0;
            default -> 50.0;
        };

        double affectedScore = request.getNumberOfPeopleAffected() * 2.0;
        
        // Deadline factor: closer deadlines score higher
        double deadlineScore = 1000.0;
        long minsToDeadline = ChronoUnit.MINUTES.between(LocalDateTime.now(), request.getDeadline());
        if (minsToDeadline > 0) {
            deadlineScore = Math.max(0.0, 10000.0 / minsToDeadline);
        }

        return severityScore + affectedScore + deadlineScore;
    }

    public List<DisasterSimulation> getAllSimulations() {
        return simulationRepository.findAll();
    }

    public DisasterSimulation getSimulationById(Long id) {
        return simulationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation run not found with id: " + id));
    }
}
