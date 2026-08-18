package com.resqflow.domain.vehicle;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("BOAT")
public class Boat extends Vehicle {
    @Override
    public boolean supportsRouteType(String routeType) {
        return "WATER".equalsIgnoreCase(routeType);
    }
}
