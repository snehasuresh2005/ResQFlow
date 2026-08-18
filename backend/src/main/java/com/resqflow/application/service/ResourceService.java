package com.resqflow.application.service;

import com.resqflow.api.resources.CreateResourceDto;
import com.resqflow.api.resources.ResourceAvailabilityDto;
import com.resqflow.api.resources.ResourceDto;
import com.resqflow.application.factory.ResourceFactory;
import com.resqflow.common.exception.ResourceNotFoundException;
import com.resqflow.domain.location.Depot;
import com.resqflow.domain.resource.EquipmentResource;
import com.resqflow.domain.resource.FoodResource;
import com.resqflow.domain.resource.MedicalResource;
import com.resqflow.domain.resource.Resource;
import com.resqflow.infrastructure.persistence.DepotRepository;
import com.resqflow.infrastructure.persistence.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final DepotRepository depotRepository;

    public ResourceService(ResourceRepository resourceRepository, DepotRepository depotRepository) {
        this.resourceRepository = resourceRepository;
        this.depotRepository = depotRepository;
    }

    public ResourceDto createResource(CreateResourceDto dto) {
        Depot depot = depotRepository.findById(dto.getDepotId())
                .orElseThrow(() -> new ResourceNotFoundException("Depot not found with id: " + dto.getDepotId()));
        
        Resource resource = ResourceFactory.createResource(dto, depot);
        Resource saved = resourceRepository.save(resource);
        return mapToDto(saved);
    }

    public ResourceDto updateResource(Long id, CreateResourceDto dto) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        Depot depot = depotRepository.findById(dto.getDepotId())
                .orElseThrow(() -> new ResourceNotFoundException("Depot not found with id: " + dto.getDepotId()));

        resource.setName(dto.getName());
        resource.setQuantity(dto.getQuantity());
        resource.setUnit(dto.getUnit());
        resource.setDepot(depot);
        resource.setExpiryDate(dto.getExpiryDate());
        resource.setPriority(dto.getPriority());
        resource.setWeightPerUnit(dto.getWeightPerUnit());
        resource.setVolumePerUnit(dto.getVolumePerUnit());

        if (resource instanceof FoodResource food) {
            food.setStorageRequirement(dto.getStorageRequirement());
        } else if (resource instanceof MedicalResource medical) {
            medical.setTempConstraint(dto.getTempConstraint());
        } else if (resource instanceof EquipmentResource equipment) {
            equipment.setIsReusable(dto.getIsReusable());
        }

        Resource updated = resourceRepository.save(resource);
        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public ResourceDto getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        return mapToDto(resource);
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> getAllResources() {
        return resourceRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResourceAvailabilityDto> getAvailability() {
        List<Resource> resources = resourceRepository.findAll();
        
        // Group by type and unit to aggregate properly
        Map<String, Double> availabilityMap = resources.stream()
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .collect(Collectors.groupingBy(
                        Resource::getResourceType,
                        Collectors.summingDouble(Resource::getQuantity)
                ));

        return availabilityMap.entrySet().stream()
                .map(entry -> {
                    String unit = resources.stream()
                            .filter(r -> r.getResourceType().equals(entry.getKey()))
                            .map(Resource::getUnit)
                            .findFirst()
                            .orElse("units");
                    return new ResourceAvailabilityDto(entry.getKey(), entry.getValue(), unit);
                })
                .collect(Collectors.toList());
    }

    public ResourceDto mapToDto(Resource resource) {
        Double tempConstraint = null;
        String storageRequirement = null;
        Boolean isReusable = null;

        if (resource instanceof FoodResource food) {
            storageRequirement = food.getStorageRequirement();
        } else if (resource instanceof MedicalResource medical) {
            tempConstraint = medical.getTempConstraint();
        } else if (resource instanceof EquipmentResource equipment) {
            isReusable = equipment.getIsReusable();
        }

        return ResourceDto.builder()
                .id(resource.getId())
                .name(resource.getName())
                .resourceType(resource.getResourceType())
                .quantity(resource.getQuantity())
                .unit(resource.getUnit())
                .depotId(resource.getDepot() != null ? resource.getDepot().getId() : null)
                .depotName(resource.getDepot() != null ? resource.getDepot().getName() : null)
                .expiryDate(resource.getExpiryDate())
                .priority(resource.getPriority())
                .weightPerUnit(resource.getWeightPerUnit())
                .volumePerUnit(resource.getVolumePerUnit())
                .status(resource.getStatus())
                .tempConstraint(tempConstraint)
                .storageRequirement(storageRequirement)
                .isReusable(isReusable)
                .version(resource.getVersion())
                .build();
    }
}
