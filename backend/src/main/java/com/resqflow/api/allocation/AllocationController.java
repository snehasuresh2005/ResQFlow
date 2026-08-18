package com.resqflow.api.allocation;

import com.resqflow.application.service.AllocationService;
import com.resqflow.common.validation.Idempotent;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/allocation")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping("/allocate")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    @Idempotent
    public ResponseEntity<AllocationResponseDto> allocate(@Valid @RequestBody AllocateResourcesRequest request) {
        AllocationResponseDto response = allocationService.allocateResources(request.getRequestId(), request.getStrategy());
        return ResponseEntity.ok(response);
    }
}
