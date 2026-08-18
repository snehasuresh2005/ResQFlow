package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.allocation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByRequestId(Long requestId);
    List<Reservation> findByStatusAndExpiresAtBefore(String status, LocalDateTime time);
}
