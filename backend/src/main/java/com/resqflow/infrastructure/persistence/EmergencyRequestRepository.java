package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.request.EmergencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {
    Optional<EmergencyRequest> findByRequestNumber(String requestNumber);
    List<EmergencyRequest> findByStatus(String status);
}
