package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.resource.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Resource r WHERE r.resourceType = :resourceType AND r.status = 'AVAILABLE'")
    List<Resource> findAvailableByResourceType(@Param("resourceType") String resourceType);
    
    List<Resource> findByDepotId(Long depotId);
}
