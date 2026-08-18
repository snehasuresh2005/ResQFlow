package com.resqflow.application.strategy;

import com.resqflow.domain.resource.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationProposal {
    private Resource resource;
    private Double quantity;
    private Double score;
}
