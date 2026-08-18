package com.resqflow.api.routing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoutingRequestDto {
    @NotBlank
    private String startType; // DEPOT, ZONE, SHELTER, INTERSECTION
    @NotNull
    private Long startId;
    @NotBlank
    private String endType;
    @NotNull
    private Long endId;
    @NotBlank
    private String strategy; // DIJKSTRA, ASTAR
}
