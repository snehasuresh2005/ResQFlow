package com.resqflow.api.allocation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AllocationResponseDto {
    private Long requestId;
    private String requestNumber;
    private boolean fullyAllocated;
    private List<AllocationDetailDto> allocations;
    private boolean missionCreated;
    private Long missionId;

    @Data
    @Builder
    public static class AllocationDetailDto {
        private Long allocationId;
        private Long resourceId;
        private String resourceName;
        private Double quantity;
        private Long depotId;
    }
}
