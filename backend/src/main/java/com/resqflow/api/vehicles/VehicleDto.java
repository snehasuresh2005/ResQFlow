package com.resqflow.api.vehicles;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleDto {
    private Long id;
    private String registrationNumber;
    private Double capacityWeight;
    private Double capacityVolume;
    private Double fuelLevel;
    private Double currentLatitude;
    private Double currentLongitude;
    private String status;
    private String vehicleType;
    private Long version;
}
