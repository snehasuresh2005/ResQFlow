package com.resqflow.domain.vehicle;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("AMBULANCE")
public class Ambulance extends Vehicle {
    @Override
    public boolean supportsRouteType(String routeType) {
        return "LAND".equalsIgnoreCase(routeType);
    }
}
