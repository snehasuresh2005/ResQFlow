package com.resqflow.api.routing;

import com.resqflow.application.service.IncidentService;
import com.resqflow.application.service.RoutingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routing")
public class RoutingController {

    private final RoutingService routingService;
    private final IncidentService incidentService;

    public RoutingController(RoutingService routingService, IncidentService incidentService) {
        this.routingService = routingService;
        this.incidentService = incidentService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<RoutingResponseDto> calculateRoute(@Valid @RequestBody RoutingRequestDto request) {
        RoutingResponseDto response = routingService.calculateRoute(
                request.getStartType(),
                request.getStartId(),
                request.getEndType(),
                request.getEndId(),
                request.getStrategy()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reroute")
    public ResponseEntity<String> triggerReroute(@RequestParam Long roadId) {
        incidentService.blockRoad(roadId);
        return ResponseEntity.ok("Road blocked successfully and affected missions rerouted.");
    }
}
