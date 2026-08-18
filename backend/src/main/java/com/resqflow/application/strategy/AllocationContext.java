package com.resqflow.application.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationContext {
    @Builder.Default
    private double urgencyWeight = 0.3;
    @Builder.Default
    private double proximityWeight = 0.25;
    @Builder.Default
    private double expiryWeight = 0.2;
    @Builder.Default
    private double quantityWeight = 0.15;
    @Builder.Default
    private double deadlineWeight = 0.1;
}
