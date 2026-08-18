package com.resqflow.application.routing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphNode {
    private String key; // e.g. "DEPOT_1", "ZONE_2"
    private String type; // DEPOT, ZONE, SHELTER, INTERSECTION
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
}
