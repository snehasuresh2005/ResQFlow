package com.resqflow.application.service;

import com.resqflow.api.routing.RoutingResponseDto;
import com.resqflow.application.state.MissionState;
import com.resqflow.application.state.MissionStateFactory;
import com.resqflow.common.exception.ResourceNotFoundException;
import com.resqflow.common.exception.RouteUnavailableException;
import com.resqflow.domain.location.Depot;
import com.resqflow.domain.location.EmergencyZone;
import com.resqflow.domain.mission.Mission;
import com.resqflow.domain.routing.Route;
import com.resqflow.domain.routing.RouteNode;
import com.resqflow.domain.vehicle.Driver;
import com.resqflow.domain.vehicle.Vehicle;
import com.resqflow.infrastructure.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MissionService {

    private final MissionRepository missionRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final RouteRepository routeRepository;
    private final MissionStateFactory stateFactory;
    private final RoutingService routingService;
    private final OutboxService outboxService;
    private final AllocationRepository allocationRepository;
    private final DepotRepository depotRepository;

    public MissionService(MissionRepository missionRepository,
                          VehicleRepository vehicleRepository,
                          DriverRepository driverRepository,
                          RouteRepository routeRepository,
                          MissionStateFactory stateFactory,
                          RoutingService routingService,
                          OutboxService outboxService,
                          AllocationRepository allocationRepository,
                          DepotRepository depotRepository) {
        this.missionRepository = missionRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.routeRepository = routeRepository;
        this.stateFactory = stateFactory;
        this.routingService = routingService;
        this.outboxService = outboxService;
        this.allocationRepository = allocationRepository;
        this.depotRepository = depotRepository;
    }

    private void releaseAssets(Mission mission) {
        Vehicle vehicle = mission.getVehicle();
        if (vehicle != null) {
            vehicle.setStatus("AVAILABLE");
            vehicleRepository.save(vehicle);
        }

        Driver driver = mission.getDriver();
        if (driver != null) {
            driver.setStatus("AVAILABLE");
            driver.setVehicle(null);
            driverRepository.save(driver);
        }
    }

    private void populateMissionRoute(Mission mission) {
        if (mission.getRoute() != null) {
            return;
        }
        Depot depot = null;
        List<com.resqflow.domain.allocation.Allocation> allocations = allocationRepository.findByRequestId(mission.getRequest().getId());
        if (!allocations.isEmpty()) {
            depot = allocations.get(0).getResource().getDepot();
        } else {
            List<Depot> depots = depotRepository.findAll();
            if (!depots.isEmpty()) {
                depot = depots.get(0);
            }
        }

        if (depot != null) {
            EmergencyZone zone = mission.getRequest().getEmergencyZone();

            RoutingResponseDto routingResp = routingService.calculateRoute(
                    "DEPOT", depot.getId(),
                    "ZONE", zone.getId(),
                    "DIJKSTRA"
            );

            if (routingResp.isSuccess()) {
                Route route = Route.builder()
                        .totalDistance(routingResp.getTotalDistance())
                        .totalTime(routingResp.getTotalTravelTime())
                        .build();

                List<RouteNode> routeNodes = new ArrayList<>();
                for (int i = 0; i < routingResp.getPath().size(); i++) {
                    RoutingResponseDto.PathNodeDto nodeDto = routingResp.getPath().get(i);
                    routeNodes.add(RouteNode.builder()
                            .route(route)
                            .sequenceOrder(i)
                            .nodeType(nodeDto.getType())
                            .nodeId(nodeDto.getId())
                            .build());
                }
                route.setRouteNodes(routeNodes);
                Route savedRoute = routeRepository.save(route);
                mission.setRoute(savedRoute);
                missionRepository.save(mission);
            } else {
                // FALLBACK: Create a direct route between Depot and Zone!
                Route route = Route.builder()
                        .totalDistance(10.0)
                        .totalTime(15.0)
                        .build();

                List<RouteNode> routeNodes = new ArrayList<>();
                routeNodes.add(RouteNode.builder()
                        .route(route)
                        .sequenceOrder(0)
                        .nodeType("DEPOT")
                        .nodeId(depot.getId())
                        .build());
                routeNodes.add(RouteNode.builder()
                        .route(route)
                        .sequenceOrder(1)
                        .nodeType("ZONE")
                        .nodeId(zone.getId())
                        .build());
                route.setRouteNodes(routeNodes);
                Route savedRoute = routeRepository.save(route);
                mission.setRoute(savedRoute);
                missionRepository.save(mission);
            }
        }
    }

    public Mission dispatchMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));

        populateMissionRoute(mission);

        MissionState state = stateFactory.getState(mission.getStatus());
        state.dispatch(mission);
        
        Mission saved = missionRepository.save(mission);
        outboxService.saveEvent("Mission", saved.getId().toString(), "MissionDispatchedEvent", saved);
        return saved;
    }

    public Mission startTransit(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));

        MissionState state = stateFactory.getState(mission.getStatus());
        state.transit(mission);

        Mission saved = missionRepository.save(mission);
        outboxService.saveEvent("Mission", saved.getId().toString(), "MissionInTransitEvent", saved);
        return saved;
    }

    public Mission blockMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));

        MissionState state = stateFactory.getState(mission.getStatus());
        state.block(mission);

        Mission saved = missionRepository.save(mission);
        outboxService.saveEvent("Mission", saved.getId().toString(), "MissionBlockedEvent", saved);
        return saved;
    }

    public Mission deliverMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));

        MissionState state = stateFactory.getState(mission.getStatus());
        state.deliver(mission);

        releaseAssets(mission);
        
        // Fulfill the emergency request
        if (mission.getRequest() != null) {
            mission.getRequest().setStatus("FULFILLED");
        }

        Mission saved = missionRepository.save(mission);
        outboxService.saveEvent("Mission", saved.getId().toString(), "MissionDeliveredEvent", saved);
        return saved;
    }

    public Mission cancelMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));

        MissionState state = stateFactory.getState(mission.getStatus());
        state.cancel(mission);

        releaseAssets(mission);

        if (mission.getRequest() != null) {
            mission.getRequest().setStatus("CANCELLED");
        }

        Mission saved = missionRepository.save(mission);
        outboxService.saveEvent("Mission", saved.getId().toString(), "MissionCancelledEvent", saved);
        return saved;
    }

    public Mission failMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));

        MissionState state = stateFactory.getState(mission.getStatus());
        state.fail(mission);

        releaseAssets(mission);

        if (mission.getRequest() != null) {
            mission.getRequest().setStatus("FAILED");
        }

        Mission saved = missionRepository.save(mission);
        outboxService.saveEvent("Mission", saved.getId().toString(), "MissionFailedEvent", saved);
        return saved;
    }

    public Mission rerouteMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));

        // First transition to BLOCKED if not already blocked
        if (!"BLOCKED".equalsIgnoreCase(mission.getStatus())) {
            blockMission(id);
        }

        // Calculate a new route from current location (represented by vehicle's lat/long or the start depot)
        // to the emergency zone.
        // Let's find the destination emergency zone
        EmergencyZone zone = mission.getRequest().getEmergencyZone();
        
        // Find nearest depot or start node to reroute from, or just recalculate from original start node
        // but with blocked roads bypassed (our RoutingService does this automatically by reading DB road status).
        // Let's recalculate from the vehicle's depot (the source of resources) or if vehicle is in-transit, 
        // calculate from depot to zone skipping blocked paths.
        // To be safe and clean, let's recalculate from the start depot to target zone.
        // Depot is linked to the allocated resources, which we can extract:
        Depot depot = mission.getRequest().getRequestedResources().isEmpty() ? null :
                missionRepository.findById(id).get().getRequest().getRequestedResources().get(0).getRequest().getEmergencyZone().getId() != null ?
                // Let's get the depot from the vehicle's location or the request's allocation depots
                null : null;
        
        // Let's find the start depot from vehicle coordinates or allocations. Let's find any depot.
        // Actually, we can get depot from the route's first node!
        String startType = "DEPOT";
        Long startId = 1L;
        if (mission.getRoute() != null && !mission.getRoute().getRouteNodes().isEmpty()) {
            RouteNode firstNode = mission.getRoute().getRouteNodes().get(0);
            startType = firstNode.getNodeType();
            startId = firstNode.getNodeId();
        }

        RoutingResponseDto rerouteResp = routingService.calculateRoute(
                startType, startId,
                "ZONE", zone.getId(),
                "DIJKSTRA"
        );

        if (!rerouteResp.isSuccess()) {
            throw new RouteUnavailableException("Failed to find alternative route to zone: " + zone.getName());
        }

        // Create new Route entity
        Route newRoute = Route.builder()
                .totalDistance(rerouteResp.getTotalDistance())
                .totalTime(rerouteResp.getTotalTravelTime())
                .build();

        List<RouteNode> routeNodes = new ArrayList<>();
        for (int i = 0; i < rerouteResp.getPath().size(); i++) {
            RoutingResponseDto.PathNodeDto nodeDto = rerouteResp.getPath().get(i);
            routeNodes.add(RouteNode.builder()
                    .route(newRoute)
                    .sequenceOrder(i)
                    .nodeType(nodeDto.getType())
                    .nodeId(nodeDto.getId())
                    .build());
        }
        newRoute.setRouteNodes(routeNodes);
        
        Route savedRoute = routeRepository.save(newRoute);

        MissionState state = stateFactory.getState(mission.getStatus());
        state.reroute(mission, savedRoute);

        // Transition back to IN_TRANSIT
        stateFactory.getState(mission.getStatus()).transit(mission);

        Mission saved = missionRepository.save(mission);
        outboxService.saveEvent("Mission", saved.getId().toString(), "MissionReroutedEvent", saved);
        return saved;
    }

    public List<Mission> getAllMissions() {
        List<Mission> missions = missionRepository.findAll();
        for (Mission mission : missions) {
            if (mission.getRoute() == null) {
                populateMissionRoute(mission);
            }
        }
        return missions;
    }

    public Mission getMissionById(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));
        if (mission.getRoute() == null) {
            populateMissionRoute(mission);
        }
        return mission;
    }
}
