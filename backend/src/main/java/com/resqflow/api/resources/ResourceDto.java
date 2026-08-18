package com.resqflow.api.resources;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ResourceDto {
    private Long id;
    private String name;
    private String resourceType;
    private Double quantity;
    private String unit;
    private Long depotId;
    private String depotName;
    private LocalDate expiryDate;
    private Integer priority;
    private Double weightPerUnit;
    private Double volumePerUnit;
    private String status;
    private Double tempConstraint;
    private String storageRequirement;
    private Boolean isReusable;
    private Long version;
}
