package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByStatus(String status);
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
}
