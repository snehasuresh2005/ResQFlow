package com.resqflow.api.locations;

import com.resqflow.domain.location.Depot;
import com.resqflow.domain.location.EmergencyZone;
import com.resqflow.domain.location.Shelter;
import com.resqflow.domain.routing.Road;
import com.resqflow.infrastructure.persistence.DepotRepository;
import com.resqflow.infrastructure.persistence.EmergencyZoneRepository;
import com.resqflow.infrastructure.persistence.RoadRepository;
import com.resqflow.infrastructure.persistence.ShelterRepository;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationsController {

    private final DepotRepository depotRepository;
    private final ShelterRepository shelterRepository;
    private final EmergencyZoneRepository zoneRepository;
    private final RoadRepository roadRepository;

    public LocationsController(DepotRepository depotRepository,
                               ShelterRepository shelterRepository,
                               EmergencyZoneRepository zoneRepository,
                               RoadRepository roadRepository) {
        this.depotRepository = depotRepository;
        this.shelterRepository = shelterRepository;
        this.zoneRepository = zoneRepository;
        this.roadRepository = roadRepository;
    }

    @GetMapping
    public ResponseEntity<LocationsPayload> getAllLocations() {
        return ResponseEntity.ok(LocationsPayload.builder()
                .depots(depotRepository.findAll())
                .shelters(shelterRepository.findAll())
                .zones(zoneRepository.findAll())
                .roads(roadRepository.findAll())
                .build());
    }

    @Data
    @Builder
    public static class LocationsPayload {
        private List<Depot> depots;
        private List<Shelter> shelters;
        private List<EmergencyZone> zones;
        private List<Road> roads;
    }
}
