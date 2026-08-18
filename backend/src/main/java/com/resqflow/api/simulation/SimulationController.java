package com.resqflow.api.simulation;

import com.resqflow.application.service.SimulationService;
import com.resqflow.domain.simulation.DisasterSimulation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/simulation")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/run")
    public ResponseEntity<SimulationResultDto> runSimulation(@Valid @RequestBody SimulationRequestDto request) {
        SimulationResultDto result = simulationService.runSimulation(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<DisasterSimulation>> getAllSimulations() {
        return ResponseEntity.ok(simulationService.getAllSimulations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisasterSimulation> getSimulationById(@PathVariable Long id) {
        return ResponseEntity.ok(simulationService.getSimulationById(id));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<DisasterSimulation> getSimulationResults(@PathVariable Long id) {
        return ResponseEntity.ok(simulationService.getSimulationById(id));
    }
}
