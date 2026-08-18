package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.simulation.DisasterSimulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisasterSimulationRepository extends JpaRepository<DisasterSimulation, Long> {
}
