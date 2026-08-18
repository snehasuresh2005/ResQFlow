package com.resqflow.api.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateRequestDto {
    @NotNull
    private Long emergencyZoneId;

    @NotBlank
    private String requestType; // FOOD, WATER, MEDICAL, SHELTER, EVACUATION, EQUIPMENT

    @NotBlank
    private String priority; // CRITICAL, HIGH, MEDIUM, LOW

    @NotNull
    @Min(1)
    private Integer numberOfPeopleAffected;

    @NotNull
    private LocalDateTime deadline;

    @NotEmpty
    private List<RequirementDto> requirements;

    @Data
    public static class RequirementDto {
        @NotBlank
        private String resourceType; // FOOD, WATER, MEDICAL, SHELTER, EQUIPMENT

        @NotNull
        @Min(0)
        private Double quantity;

        @NotBlank
        private String unit;
    }
}
