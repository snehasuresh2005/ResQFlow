package com.resqflow.application.service;

import com.resqflow.api.routing.RoutingResponseDto;
import com.resqflow.application.routing.GraphNode;
import com.resqflow.application.routing.RouteResult;
import com.resqflow.application.routing.RoutingGraph;
import com.resqflow.application.routing.RoutingStrategy;
import com.resqflow.domain.location.Depot;
import com.resqflow.domain.location.EmergencyZone;
import com.resqflow.domain.location.Shelter;
import com.resqflow.domain.routing.Road;
import com.resqflow.infrastructure.persistence.DepotRepository;
import com.resqflow.infrastructure.persistence.EmergencyZoneRepository;
import com.resqflow.infrastructure.persistence.RoadRepository;
import com.resqflow.infrastructure.persistence.ShelterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RoutingService {

    private final RoadRepository roadRepository;
    private final DepotRepository depotRepository;
    private final EmergencyZoneRepository zoneRepository;
    private final ShelterRepository shelterRepository;
    private final Map<String, RoutingStrategy> strategies;

    public RoutingService(RoadRepository roadRepository,
                          DepotRepository depotRepository,
                          EmergencyZoneRepository zoneRepository,
                          ShelterRepository shelterRepository,
                          Map<String, RoutingStrategy> strategies) {
        this.roadRepository = roadRepository;
        this.depotRepository = depotRepository;
        this.zoneRepository = zoneRepository;
        this.shelterRepository = shelterRepository;
        this.strategies = strategies;
    }

    public RoutingGraph buildGraph() {
        RoutingGraph graph = new RoutingGraph();

        // 1. Add Depots
        for (Depot depot : depotRepository.findAll()) {
            GraphNode node = GraphNode.builder()
                    .key("DEPOT_" + depot.getId())
                    .type("DEPOT")
                    .id(depot.getId())
                    .name(depot.getName())
                    .latitude(depot.getLatitude())
                    .longitude(depot.getLongitude())
                    .build();
            graph.addNode(node);
        }

        // 2. Add Zones
        for (EmergencyZone zone : zoneRepository.findAll()) {
            GraphNode node = GraphNode.builder()
                    .key("ZONE_" + zone.getId())
                    .type("ZONE")
                    .id(zone.getId())
                    .name(zone.getName())
                    .latitude(zone.getLatitude())
                    .longitude(zone.getLongitude())
                    .build();
            graph.addNode(node);
        }

        // 3. Add Shelters
        for (Shelter shelter : shelterRepository.findAll()) {
            GraphNode node = GraphNode.builder()
                    .key("SHELTER_" + shelter.getId())
                    .type("SHELTER")
                    .id(shelter.getId())
                    .name(shelter.getName())
                    .latitude(shelter.getLatitude())
                    .longitude(shelter.getLongitude())
                    .build();
            graph.addNode(node);
        }

        // 4. Add Roads (Bidirectional)
        for (Road road : roadRepository.findAll()) {
            String fromKey = road.getStartNodeType().toUpperCase() + "_" + road.getStartNodeId();
            String toKey = road.getEndNodeType().toUpperCase() + "_" + road.getEndNodeId();

            graph.addEdge(fromKey, toKey, road.getDistance(), road.getTravelTime(), road.getStatus(), road.getId());
            graph.addEdge(toKey, fromKey, road.getDistance(), road.getTravelTime(), road.getStatus(), road.getId());
        }

        return graph;
    }

    public RoutingResponseDto calculateRoute(String startType, Long startId, String endType, Long endId, String strategyName) {
        RoutingGraph graph = buildGraph();
        String startKey = startType.toUpperCase() + "_" + startId;
        String endKey = endType.toUpperCase() + "_" + endId;

        String strategyKey = strategyName.toUpperCase();
        if (strategyKey.contains("ASTAR")) {
            strategyKey = "ASTAR";
        } else {
            strategyKey = "DIJKSTRA";
        }

        RoutingStrategy strategy = strategies.get(strategyKey);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown routing strategy: " + strategyName);
        }

        RouteResult result = strategy.calculateRoute(startKey, endKey, graph);

        if (!result.isSuccess()) {
            return RoutingResponseDto.builder().success(false).build();
        }

        List<RoutingResponseDto.PathNodeDto> pathNodes = result.getPath().stream()
                .map(node -> RoutingResponseDto.PathNodeDto.builder()
                        .type(node.getType())
                        .id(node.getId())
                        .name(node.getName())
                        .latitude(node.getLatitude())
                        .longitude(node.getLongitude())
                        .key(node.getKey())
                        .build())
                .collect(Collectors.toList());

        return RoutingResponseDto.builder()
                .path(pathNodes)
                .totalDistance(result.getTotalDistance())
                .totalTravelTime(result.getTotalTravelTime())
                .roadIds(result.getRoadIds())
                .success(true)
                .build();
    }
}
