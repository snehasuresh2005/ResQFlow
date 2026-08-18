package com.resqflow.api.allocation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AllocateResourcesRequest {
    @NotNull
    private Long requestId;

    @NotBlank
    private String strategy; // NEAREST, HIGHEST_PRIORITY, EXPIRY_AWARE, FAIR_DISTRIBUTION, HYBRID
}
