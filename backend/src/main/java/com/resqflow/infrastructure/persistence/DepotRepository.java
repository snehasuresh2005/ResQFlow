package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.location.Depot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepotRepository extends JpaRepository<Depot, Long> {
}
