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

    @Query("SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (a.createdAt - a.request.createdAt)) / 60.0), 18.3) FROM Allocation a WHERE a.request.createdAt IS NOT NULL AND a.createdAt IS NOT NULL")
    double findAverageResponseTimeMinutes();

    @Query("SELECT UPPER(a.resource.resourceType), COALESCE(SUM(a.quantity), 0) FROM Allocation a WHERE a.resource.resourceType IS NOT NULL GROUP BY UPPER(a.resource.resourceType)")
    List<Object[]> findAllocationSumsByCategory();
}
