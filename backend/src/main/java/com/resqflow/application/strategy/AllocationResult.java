package com.resqflow.application.strategy;

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
public class AllocationResult {
    @Builder.Default
    private List<AllocationProposal> proposals = new ArrayList<>();
    private boolean fullyAllocated;
    private Double allocatedQuantity;
    private Double remainingNeeded;
}
