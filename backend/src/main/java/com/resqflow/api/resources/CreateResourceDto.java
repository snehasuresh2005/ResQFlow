package com.resqflow.api.resources;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateResourceDto {
    @NotBlank
    private String name;

    @NotBlank
    private String resourceType; // FOOD, WATER, MEDICAL, SHELTER, EQUIPMENT

    @NotNull
    @Min(0)
    private Double quantity;

    @NotBlank
    private String unit;

    @NotNull
    private Long depotId;

    private LocalDate expiryDate;

    private Integer priority = 0;

    @NotNull
    @Min(0)
    private Double weightPerUnit;

    @NotNull
    @Min(0)
    private Double volumePerUnit;

    // Optional typed attributes
    private Double tempConstraint; // Medical
    private String storageRequirement; // Food
    private Boolean isReusable; // Equipment
}
