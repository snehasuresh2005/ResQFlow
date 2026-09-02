package com.resqflow.api.resources;

import com.resqflow.application.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourcesController {

    private final ResourceService resourceService;

    public ResourcesController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ResponseEntity<ResourceDto> createResource(@Valid @RequestBody CreateResourceDto dto) {
        ResourceDto created = resourceService.createResource(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ResponseEntity<ResourceDto> updateResource(@PathVariable Long id, @Valid @RequestBody CreateResourceDto dto) {
        return ResponseEntity.ok(resourceService.updateResource(id, dto));
    }

    @GetMapping
    public ResponseEntity<List<ResourceDto>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    @GetMapping("/availability")
    public ResponseEntity<List<ResourceAvailabilityDto>> getAvailability() {
        return ResponseEntity.ok(resourceService.getAvailability());
    }
}
