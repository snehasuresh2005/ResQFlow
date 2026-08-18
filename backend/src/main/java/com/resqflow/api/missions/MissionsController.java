package com.resqflow.api.missions;

import com.resqflow.application.service.MissionService;
import com.resqflow.domain.mission.Mission;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/missions")
public class MissionsController {

    private final MissionService missionService;

    public MissionsController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    public ResponseEntity<List<Mission>> getAllMissions() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mission> getMissionById(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.getMissionById(id));
    }

    @PostMapping("/{id}/dispatch")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<Mission> dispatchMission(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.dispatchMission(id));
    }

    @PostMapping("/{id}/transit")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<Mission> startTransit(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.startTransit(id));
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<Mission> blockMission(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.blockMission(id));
    }

    @PostMapping("/{id}/reroute")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<Mission> rerouteMission(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.rerouteMission(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<Mission> completeMission(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.deliverMission(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<Mission> cancelMission(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.cancelMission(id));
    }
}
