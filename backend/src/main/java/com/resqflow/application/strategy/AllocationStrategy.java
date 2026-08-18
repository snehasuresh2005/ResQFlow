package com.resqflow.application.strategy;

import com.resqflow.domain.request.EmergencyRequest;
import com.resqflow.domain.resource.Resource;

import java.util.List;

public interface AllocationStrategy {
    AllocationResult allocate(
            EmergencyRequest request,
            String resourceType,
            Double quantityNeeded,
            List<Resource> resources,
            AllocationContext context
    );
}
