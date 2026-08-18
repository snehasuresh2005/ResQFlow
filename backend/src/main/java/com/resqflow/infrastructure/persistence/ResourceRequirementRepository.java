package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.request.ResourceRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRequirementRepository extends JpaRepository<ResourceRequirement, Long> {
}
