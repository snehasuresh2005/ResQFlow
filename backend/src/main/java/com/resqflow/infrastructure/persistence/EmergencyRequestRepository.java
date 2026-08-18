package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.request.EmergencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {
    Optional<EmergencyRequest> findByRequestNumber(String requestNumber);
    List<EmergencyRequest> findByStatus(String status);

    long countByPriorityIgnoreCase(String priority);

    long countByStatusIgnoreCaseIn(List<String> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM EmergencyRequest r WHERE r.id = :id")
    Optional<EmergencyRequest> findByIdForUpdate(@Param("id") Long id);
}
