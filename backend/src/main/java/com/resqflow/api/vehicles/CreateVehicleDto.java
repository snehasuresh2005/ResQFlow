package com.resqflow.api.vehicles;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVehicleDto {
    @NotBlank
    private String registrationNumber;

    @NotNull
    @Min(0)
    private Double capacityWeight;

    @NotNull
    @Min(0)
    private Double capacityVolume;

    @NotNull
    @Min(0)
    private Double fuelLevel;

    @NotNull
    private Double currentLatitude;

    @NotNull
    private Double currentLongitude;

    @NotBlank
    private String vehicleType; // TRUCK, VAN, AMBULANCE, BOAT
}
