package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.allocation.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByRequestId(Long requestId);
    List<Allocation> findByResourceId(Long resourceId);
}
