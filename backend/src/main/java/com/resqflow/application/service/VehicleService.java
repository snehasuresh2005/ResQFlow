package com.resqflow.application.service;

import com.resqflow.api.vehicles.CreateVehicleDto;
import com.resqflow.api.vehicles.VehicleDto;
import com.resqflow.application.factory.VehicleFactory;
import com.resqflow.common.exception.ResourceNotFoundException;
import com.resqflow.domain.vehicle.Vehicle;
import com.resqflow.infrastructure.persistence.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public VehicleDto createVehicle(CreateVehicleDto dto) {
        if (vehicleRepository.findByRegistrationNumber(dto.getRegistrationNumber()).isPresent()) {
            throw new IllegalArgumentException("Registration number already exists: " + dto.getRegistrationNumber());
        }

        Vehicle vehicle = VehicleFactory.createVehicle(dto);
        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToDto(saved);
    }

    public VehicleDto updateVehicle(Long id, CreateVehicleDto dto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        vehicle.setRegistrationNumber(dto.getRegistrationNumber());
        vehicle.setCapacityWeight(dto.getCapacityWeight());
        vehicle.setCapacityVolume(dto.getCapacityVolume());
        vehicle.setFuelLevel(dto.getFuelLevel());
        vehicle.setCurrentLatitude(dto.getCurrentLatitude());
        vehicle.setCurrentLongitude(dto.getCurrentLongitude());

        Vehicle updated = vehicleRepository.save(vehicle);
        return mapToDto(updated);
    }

    public VehicleDto updateStatus(Long id, String status) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        vehicle.setStatus(status.toUpperCase());
        Vehicle updated = vehicleRepository.save(vehicle);
        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public VehicleDto getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        return mapToDto(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public VehicleDto mapToDto(Vehicle vehicle) {
        return VehicleDto.builder()
                .id(vehicle.getId())
                .registrationNumber(vehicle.getRegistrationNumber())
                .capacityWeight(vehicle.getCapacityWeight())
                .capacityVolume(vehicle.getCapacityVolume())
                .fuelLevel(vehicle.getFuelLevel())
                .currentLatitude(vehicle.getCurrentLatitude())
                .currentLongitude(vehicle.getCurrentLongitude())
                .status(vehicle.getStatus())
                .vehicleType(vehicle.getVehicleType())
                .version(vehicle.getVersion())
                .build();
    }
}
