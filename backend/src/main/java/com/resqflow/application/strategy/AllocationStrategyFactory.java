package com.resqflow.application.strategy;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AllocationStrategyFactory {

    private final Map<String, AllocationStrategy> strategies;

    public AllocationStrategyFactory(Map<String, AllocationStrategy> strategies) {
        this.strategies = strategies;
    }

    public AllocationStrategy getStrategy(String strategyName) {
        String key = strategyName.toUpperCase();
        // Fallback checks for common strategy variations
        if (key.contains("NEAREST")) key = "NEAREST";
        else if (key.contains("EXPIRY")) key = "EXPIRY_AWARE";
        else if (key.contains("PRIORITY")) key = "HIGHEST_PRIORITY";
        else if (key.contains("FAIR")) key = "FAIR_DISTRIBUTION";
        else if (key.contains("HYBRID")) key = "HYBRID";

        AllocationStrategy strategy = strategies.get(key);
        if (strategy == null) {
            throw new IllegalArgumentException("Allocation strategy not found for: " + strategyName);
        }
        return strategy;
    }
}
