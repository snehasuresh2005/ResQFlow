package com.resqflow.domain.vehicle;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TRUCK")
public class Truck extends Vehicle {
    @Override
    public boolean supportsRouteType(String routeType) {
        return "LAND".equalsIgnoreCase(routeType);
    }
}
