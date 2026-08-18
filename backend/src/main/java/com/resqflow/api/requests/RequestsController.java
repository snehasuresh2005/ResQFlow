package com.resqflow.api.requests;

import com.resqflow.application.service.EmergencyRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestsController {

    private final EmergencyRequestService requestService;

    public RequestsController(EmergencyRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<EmergencyRequestDto> createRequest(@Valid @RequestBody CreateRequestDto dto) {
        EmergencyRequestDto created = requestService.createRequest(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmergencyRequestDto>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyRequestDto> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<EmergencyRequestDto> cancelRequest(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.cancelRequest(id));
    }
}
