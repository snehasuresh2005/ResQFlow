package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.mission.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findByStatus(String status);
    List<Mission> findByDriverId(Long driverId);
    List<Mission> findByVehicleId(Long vehicleId);

    long countByStatusIgnoreCaseNotIn(List<String> excludedStatuses);

    long countByStatusIgnoreCase(String status);
}
