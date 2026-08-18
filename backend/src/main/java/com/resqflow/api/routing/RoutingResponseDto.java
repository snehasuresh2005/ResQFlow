package com.resqflow.api.routing;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoutingResponseDto {
    private List<PathNodeDto> path;
    private double totalDistance;
    private double totalTravelTime;
    private List<Long> roadIds;
    private boolean success;

    @Data
    @Builder
    public static class PathNodeDto {
        private String type;
        private Long id;
        private String name;
        private Double latitude;
        private Double longitude;
        private String key;
    }
}
