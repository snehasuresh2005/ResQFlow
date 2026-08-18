package com.resqflow.application.routing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResult {
    @Builder.Default
    private List<GraphNode> path = new ArrayList<>();
    private double totalDistance;
    private double totalTravelTime; // in minutes
    @Builder.Default
    private List<Long> roadIds = new ArrayList<>();
    private boolean success;
}
