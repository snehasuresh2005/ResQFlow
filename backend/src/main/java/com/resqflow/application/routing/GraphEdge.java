package com.resqflow.application.routing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphEdge {
    private GraphNode target;
    private double distance;
    private double travelTime;
    private String status; // OPEN, BLOCKED, RESTRICTED
    private Long roadId;
}
