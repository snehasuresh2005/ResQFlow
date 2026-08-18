package com.resqflow.api.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceAvailabilityDto {
    private String resourceType;
    private Double totalQuantity;
    private String unit;
}
