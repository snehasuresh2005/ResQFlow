package com.resqflow.application.service;

import com.resqflow.common.exception.ResourceNotFoundException;
import com.resqflow.domain.mission.Mission;
import com.resqflow.domain.routing.Road;
import com.resqflow.domain.routing.RouteNode;
import com.resqflow.infrastructure.persistence.MissionRepository;
import com.resqflow.infrastructure.persistence.RoadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class IncidentService {
    private static final Logger logger = LoggerFactory.getLogger(IncidentService.class);

    private final RoadRepository roadRepository;
    private final MissionRepository missionRepository;
    private final MissionService missionService;
    private final OutboxService outboxService;

    public IncidentService(RoadRepository roadRepository,
                           MissionRepository missionRepository,
                           MissionService missionService,
                           OutboxService outboxService) {
        this.roadRepository = roadRepository;
        this.missionRepository = missionRepository;
        this.missionService = missionService;
        this.outboxService = outboxService;
    }

    public void blockRoad(Long roadId) {
        Road road = roadRepository.findById(roadId)
                .orElseThrow(() -> new ResourceNotFoundException("Road not found with id: " + roadId));

        road.setStatus("BLOCKED");
        roadRepository.save(road);

        logger.warn("Road blockage detected on Road: {} (ID: {}). Broadcasting incident.", road.getName(), roadId);

        // Publish event
        outboxService.saveEvent("Road", roadId.toString(), "RoadBlockedEvent", road);

        // Identify affected missions
        List<Mission> activeMissions = missionRepository.findByStatus("IN_TRANSIT");
        List<Mission> affectedMissions = new ArrayList<>();

        for (Mission mission : activeMissions) {
            if (isMissionAffectedByRoad(mission, road)) {
                affectedMissions.add(mission);
            }
        }

        logger.info("Found {} active missions affected by the road blockage.", affectedMissions.size());

        // Reroute each affected mission
        for (Mission mission : affectedMissions) {
            try {
                logger.info("Initiating dynamic rerouting workflow for Mission ID: {}", mission.getId());
                missionService.rerouteMission(mission.getId());
            } catch (Exception e) {
                logger.error("Failed to automatically reroute Mission ID: {}. Transitioning to FAILED.", mission.getId(), e);
                missionService.failMission(mission.getId());
            }
        }
    }

    private boolean isMissionAffectedByRoad(Mission mission, Road road) {
        if (mission.getRoute() == null || mission.getRoute().getRouteNodes().isEmpty()) {
            return false;
        }

        List<RouteNode> nodes = mission.getRoute().getRouteNodes();
        
        // Traverse adjacent pairs of route nodes to see if they match the road endpoints
        for (int i = 0; i < nodes.size() - 1; i++) {
            RouteNode curr = nodes.get(i);
            RouteNode next = nodes.get(i + 1);

            boolean matchForward = curr.getNodeType().equalsIgnoreCase(road.getStartNodeType())
                    && curr.getNodeId().equals(road.getStartNodeId())
                    && next.getNodeType().equalsIgnoreCase(road.getEndNodeType())
                    && next.getNodeId().equals(road.getEndNodeId());

            boolean matchBackward = curr.getNodeType().equalsIgnoreCase(road.getEndNodeType())
                    && curr.getNodeId().equals(road.getEndNodeId())
                    && next.getNodeType().equalsIgnoreCase(road.getStartNodeType())
                    && next.getNodeId().equals(road.getStartNodeId());

            if (matchForward || matchBackward) {
                return true;
            }
        }

        return false;
    }
}
