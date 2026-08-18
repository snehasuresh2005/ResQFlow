package com.resqflow.application.factory;

import com.resqflow.api.vehicles.CreateVehicleDto;
import com.resqflow.domain.vehicle.*;

public class VehicleFactory {

    public static Vehicle createVehicle(CreateVehicleDto dto) {
        Vehicle vehicle;
        String type = dto.getVehicleType().toUpperCase();

        switch (type) {
            case "TRUCK" -> vehicle = new Truck();
            case "VAN" -> vehicle = new Van();
            case "AMBULANCE" -> vehicle = new Ambulance();
            case "BOAT" -> vehicle = new Boat();
            default -> throw new IllegalArgumentException("Unknown vehicle type: " + dto.getVehicleType());
        }

        vehicle.setRegistrationNumber(dto.getRegistrationNumber());
        vehicle.setCapacityWeight(dto.getCapacityWeight());
        vehicle.setCapacityVolume(dto.getCapacityVolume());
        vehicle.setFuelLevel(dto.getFuelLevel());
        vehicle.setCurrentLatitude(dto.getCurrentLatitude());
        vehicle.setCurrentLongitude(dto.getCurrentLongitude());
        vehicle.setStatus("AVAILABLE");

        return vehicle;
    }
}
