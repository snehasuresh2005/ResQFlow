package com.resqflow.application.factory;

import com.resqflow.api.resources.CreateResourceDto;
import com.resqflow.domain.location.Depot;
import com.resqflow.domain.resource.*;

public class ResourceFactory {

    public static Resource createResource(CreateResourceDto dto, Depot depot) {
        Resource resource;
        String type = dto.getResourceType().toUpperCase();
        
        switch (type) {
            case "FOOD" -> {
                FoodResource food = new FoodResource();
                food.setStorageRequirement(dto.getStorageRequirement() != null ? dto.getStorageRequirement() : "DRY");
                resource = food;
            }
            case "WATER" -> resource = new WaterResource();
            case "MEDICAL" -> {
                MedicalResource medical = new MedicalResource();
                medical.setTempConstraint(dto.getTempConstraint() != null ? dto.getTempConstraint() : 25.0);
                resource = medical;
            }
            case "SHELTER" -> resource = new ShelterKitResource();
            case "EQUIPMENT" -> {
                EquipmentResource equipment = new EquipmentResource();
                equipment.setIsReusable(dto.getIsReusable() != null ? dto.getIsReusable() : false);
                resource = equipment;
            }
            default -> throw new IllegalArgumentException("Unknown resource type: " + dto.getResourceType());
        }

        resource.setName(dto.getName());
        resource.setQuantity(dto.getQuantity());
        resource.setUnit(dto.getUnit());
        resource.setDepot(depot);
        resource.setExpiryDate(dto.getExpiryDate());
        resource.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        resource.setWeightPerUnit(dto.getWeightPerUnit());
        resource.setVolumePerUnit(dto.getVolumePerUnit());
        resource.setStatus("AVAILABLE");
        
        return resource;
    }
}
