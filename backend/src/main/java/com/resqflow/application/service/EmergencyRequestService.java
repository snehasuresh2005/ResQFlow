package com.resqflow.application.service;

import com.resqflow.api.requests.CreateRequestDto;
import com.resqflow.api.requests.EmergencyRequestDto;
import com.resqflow.common.exception.ResourceNotFoundException;
import com.resqflow.domain.location.EmergencyZone;
import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.request.ResourceRequirement;
import com.resqflow.infrastructure.persistence.EmergencyRequestRepository;
import com.resqflow.infrastructure.persistence.EmergencyZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmergencyRequestService {

    private final EmergencyRequestRepository requestRepository;
    private final EmergencyZoneRepository zoneRepository;
    private final OutboxService outboxService;

    public EmergencyRequestService(EmergencyRequestRepository requestRepository, 
                                   EmergencyZoneRepository zoneRepository,
                                   OutboxService outboxService) {
        this.requestRepository = requestRepository;
        this.zoneRepository = zoneRepository;
        this.outboxService = outboxService;
    }

    public EmergencyRequestDto createRequest(CreateRequestDto dto) {
        EmergencyZone zone = zoneRepository.findById(dto.getEmergencyZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Emergency zone not found with id: " + dto.getEmergencyZoneId()));

        String reqNumber = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        EmergencyRequest request = EmergencyRequest.builder()
                .requestNumber(reqNumber)
                .emergencyZone(zone)
                .requestType(dto.getRequestType().toUpperCase())
                .priority(dto.getPriority().toUpperCase())
                .numberOfPeopleAffected(dto.getNumberOfPeopleAffected())
                .deadline(dto.getDeadline())
                .status("CREATED")
                .build();

        List<ResourceRequirement> requirements = dto.getRequirements().stream()
                .map(reqDto -> ResourceRequirement.builder()
                        .request(request)
                        .resourceType(reqDto.getResourceType().toUpperCase())
                        .quantity(reqDto.getQuantity())
                        .unit(reqDto.getUnit())
                        .build())
                .collect(Collectors.toList());

        request.setRequestedResources(requirements);

        EmergencyRequest saved = requestRepository.save(request);
        EmergencyRequestDto savedDto = mapToDto(saved);

        // Write to Outbox
        outboxService.saveEvent("EmergencyRequest", saved.getId().toString(), "EmergencyRequestCreatedEvent", savedDto);

        return savedDto;
    }

    public EmergencyRequestDto cancelRequest(Long id) {
        EmergencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        request.setStatus("CANCELLED");
        EmergencyRequest saved = requestRepository.save(request);
        EmergencyRequestDto dto = mapToDto(saved);

        outboxService.saveEvent("EmergencyRequest", saved.getId().toString(), "EmergencyRequestCancelledEvent", dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public EmergencyRequestDto getRequestById(Long id) {
        EmergencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
        return mapToDto(request);
    }

    @Transactional(readOnly = true)
    public List<EmergencyRequestDto> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public EmergencyRequestDto mapToDto(EmergencyRequest request) {
        List<EmergencyRequestDto.RequirementDetailDto> reqDetails = request.getRequestedResources().stream()
                .map(req -> EmergencyRequestDto.RequirementDetailDto.builder()
                        .id(req.getId())
                        .resourceType(req.getResourceType())
                        .quantity(req.getQuantity())
                        .unit(req.getUnit())
                        .build())
                .collect(Collectors.toList());

        return EmergencyRequestDto.builder()
                .id(request.getId())
                .requestNumber(request.getRequestNumber())
                .emergencyZoneId(request.getEmergencyZone() != null ? request.getEmergencyZone().getId() : null)
                .emergencyZoneName(request.getEmergencyZone() != null ? request.getEmergencyZone().getName() : null)
                .requestType(request.getRequestType())
                .priority(request.getPriority())
                .numberOfPeopleAffected(request.getNumberOfPeopleAffected())
                .deadline(request.getDeadline())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .requirements(reqDetails)
                .build();
    }
}
