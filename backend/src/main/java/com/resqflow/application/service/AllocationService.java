package com.resqflow.application.service;

import com.resqflow.api.allocation.AllocationResponseDto;
import com.resqflow.application.rules.RuleContext;
import com.resqflow.application.rules.RuleEngine;
import com.resqflow.application.strategy.*;
import com.resqflow.common.exception.AllocationConflictException;
import com.resqflow.common.exception.ResourceNotFoundException;
import com.resqflow.domain.allocation.Allocation;
import com.resqflow.domain.mission.Mission;
import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.request.ResourceRequirement;
import com.resqflow.domain.resource.Resource;
import com.resqflow.domain.vehicle.Driver;
import com.resqflow.domain.vehicle.Vehicle;
import com.resqflow.infrastructure.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AllocationService {

    private final EmergencyRequestRepository requestRepository;
    private final ResourceRepository resourceRepository;
    private final AllocationRepository allocationRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final MissionRepository missionRepository;
    private final AllocationStrategyFactory strategyFactory;
    private final RuleEngine ruleEngine;
    private final OutboxService outboxService;

    public AllocationService(EmergencyRequestRepository requestRepository,
                             ResourceRepository resourceRepository,
                             AllocationRepository allocationRepository,
                             VehicleRepository vehicleRepository,
                             DriverRepository driverRepository,
                             MissionRepository missionRepository,
                             AllocationStrategyFactory strategyFactory,
                             RuleEngine ruleEngine,
                             OutboxService outboxService) {
        this.requestRepository = requestRepository;
        this.resourceRepository = resourceRepository;
        this.allocationRepository = allocationRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.missionRepository = missionRepository;
        this.strategyFactory = strategyFactory;
        this.ruleEngine = ruleEngine;
        this.outboxService = outboxService;
    }

    public AllocationResponseDto allocateResources(Long requestId, String strategyName) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency request not found with id: " + requestId));

        if ("FULLY_ALLOCATED".equalsIgnoreCase(request.getStatus()) || "FULFILLED".equalsIgnoreCase(request.getStatus())) {
            throw new AllocationConflictException("Request is already allocated or fulfilled: " + request.getRequestNumber());
        }

        AllocationStrategy strategy = strategyFactory.getStrategy(strategyName);
        AllocationContext context = new AllocationContext(); // Use default configured weights

        List<AllocationResponseDto.AllocationDetailDto> details = new ArrayList<>();
        boolean allRequirementsSatisfied = true;
        double totalRequested = 0.0;
        double totalAllocated = 0.0;

        for (ResourceRequirement req : request.getRequestedResources()) {
            totalRequested += req.getQuantity();
            
            // Find all matching available resources
            List<Resource> availableResources = resourceRepository.findAvailableByResourceType(req.getResourceType());
            
            // Run strategy
            AllocationResult result = strategy.allocate(request, req.getResourceType(), req.getQuantity(), availableResources, context);
            
            if (!result.isFullyAllocated()) {
                allRequirementsSatisfied = false;
            }

            totalAllocated += result.getAllocatedQuantity();

            for (AllocationProposal prop : result.getProposals()) {
                Resource resource = prop.getResource();
                Double quantity = prop.getQuantity();

                // Rule Engine check
                RuleContext ruleContext = RuleContext.builder()
                        .request(request)
                        .resource(resource)
                        .quantityNeeded(quantity)
                        .build();

                if (!ruleEngine.checkAllPassed(ruleContext)) {
                    throw new AllocationConflictException("Rule check failed for resource '" + resource.getName() + "' under strategy " + strategyName);
                }

                // Decrement resource stock
                double newQuantity = resource.getQuantity() - quantity;
                resource.setQuantity(newQuantity);
                if (newQuantity <= 0.05) {
                    resource.setStatus("OUT_OF_STOCK");
                }
                resourceRepository.save(resource);

                // Create Allocation record
                Allocation allocation = Allocation.builder()
                        .request(request)
                        .resource(resource)
                        .quantity(quantity)
                        .status("ALLOCATED")
                        .build();

                Allocation savedAlloc = allocationRepository.save(allocation);

                details.add(AllocationResponseDto.AllocationDetailDto.builder()
                        .allocationId(savedAlloc.getId())
                        .resourceId(resource.getId())
                        .resourceName(resource.getName())
                        .quantity(quantity)
                        .depotId(resource.getDepot().getId())
                        .build());
            }
        }

        if (totalAllocated <= 0) {
            throw new AllocationConflictException("No resources could be allocated matching requirements.");
        }

        // Set status
        request.setStatus(allRequirementsSatisfied ? "FULLY_ALLOCATED" : "PARTIALLY_ALLOCATED");
        requestRepository.save(request);

        // Attempt Vehicle & Driver Assignment
        Vehicle assignedVehicle = null;
        Driver assignedDriver = null;
        boolean missionCreated = false;
        Long missionId = null;

        // Simple vehicle lookups
        List<Vehicle> availableVehicles = vehicleRepository.findByStatus("AVAILABLE");
        List<Driver> availableDrivers = driverRepository.findByStatus("AVAILABLE");

        if (!availableVehicles.isEmpty() && !availableDrivers.isEmpty()) {
            assignedVehicle = availableVehicles.get(0);
            assignedDriver = availableDrivers.get(0);

            assignedVehicle.setStatus("ASSIGNED");
            vehicleRepository.save(assignedVehicle);

            assignedDriver.setVehicle(assignedVehicle);
            assignedDriver.setStatus("ASSIGNED");
            driverRepository.save(assignedDriver);

            // Create Mission
            Mission mission = Mission.builder()
                    .request(request)
                    .vehicle(assignedVehicle)
                    .driver(assignedDriver)
                    .status("CREATED")
                    .build();

            Mission savedMission = missionRepository.save(mission);
            missionCreated = true;
            missionId = savedMission.getId();

            outboxService.saveEvent("Mission", missionId.toString(), "MissionCreatedEvent", savedMission);
        }

        AllocationResponseDto response = AllocationResponseDto.builder()
                .requestId(request.getId())
                .requestNumber(request.getRequestNumber())
                .fullyAllocated(allRequirementsSatisfied)
                .allocations(details)
                .missionCreated(missionCreated)
                .missionId(missionId)
                .build();

        // Write events
        outboxService.saveEvent("Allocation", request.getId().toString(), "ResourceAllocatedEvent", response);

        return response;
    }
}
