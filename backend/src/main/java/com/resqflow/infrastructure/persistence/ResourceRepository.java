package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.resource.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    @Query("SELECT r FROM Resource r WHERE r.resourceType = :resourceType AND r.status = 'AVAILABLE'")
    List<Resource> findAvailableByResourceType(@Param("resourceType") String resourceType);
    
    List<Resource> findByDepotId(Long depotId);
}
