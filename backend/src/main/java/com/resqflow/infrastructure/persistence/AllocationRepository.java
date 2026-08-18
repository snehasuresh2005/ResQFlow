package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.allocation.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByRequestId(Long requestId);
    List<Allocation> findByResourceId(Long resourceId);

    @Query(value = "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (a.created_at - r.created_at)) / 60.0), 18.3) FROM allocations a JOIN emergency_requests r ON a.request_id = r.id WHERE r.created_at IS NOT NULL AND a.created_at IS NOT NULL", nativeQuery = true)
    double findAverageResponseTimeMinutes();

    @Query(value = "SELECT UPPER(res.resource_type), COALESCE(SUM(a.quantity), 0) FROM allocations a JOIN resources res ON a.resource_id = res.id WHERE res.resource_type IS NOT NULL GROUP BY UPPER(res.resource_type)", nativeQuery = true)
    List<Object[]> findAllocationSumsByCategory();
}
