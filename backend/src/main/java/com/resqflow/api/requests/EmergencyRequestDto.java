package com.resqflow.api.requests;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EmergencyRequestDto {
    private Long id;
    private String requestNumber;
    private Long emergencyZoneId;
    private String emergencyZoneName;
    private String requestType;
    private String priority;
    private Integer numberOfPeopleAffected;
    private LocalDateTime deadline;
    private String status;
    private LocalDateTime createdAt;
    private List<RequirementDetailDto> requirements;

    @Data
    @Builder
    public static class RequirementDetailDto {
        private Long id;
        private String resourceType;
        private Double quantity;
        private String unit;
    }
}
