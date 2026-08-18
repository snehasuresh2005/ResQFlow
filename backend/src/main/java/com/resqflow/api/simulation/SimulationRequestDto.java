package com.resqflow.api.simulation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SimulationRequestDto {
    @NotBlank
    private String scenario; // FLOOD, EARTHQUAKE, WILDFIRE

    @NotNull
    @Min(1)
    private Integer requests;

    @NotNull
    @Min(1)
    private Integer depots;

    @NotNull
    @Min(1)
    private Integer vehicles;

    @NotNull
    @Min(1)
    private Integer resources;

    @NotNull
    @Min(1)
    private Integer zones;

    @NotNull
    @Min(0)
    private Integer blockedRoadPercentage;

    @NotBlank
    private String allocationStrategy; // HYBRID, NEAREST, HIGHEST_PRIORITY, EXPIRY_AWARE, FAIR_DISTRIBUTION
}
