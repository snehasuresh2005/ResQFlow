package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.location.EmergencyZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyZoneRepository extends JpaRepository<EmergencyZone, Long> {
}
