package com.resqflow.domain.vehicle;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("VAN")
public class Van extends Vehicle {
    @Override
    public boolean supportsRouteType(String routeType) {
        return "LAND".equalsIgnoreCase(routeType);
    }
}
