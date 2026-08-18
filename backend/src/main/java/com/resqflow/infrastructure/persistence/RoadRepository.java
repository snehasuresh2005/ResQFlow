package com.resqflow.infrastructure.persistence;

import com.resqflow.domain.routing.Road;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadRepository extends JpaRepository<Road, Long> {
    List<Road> findByStatus(String status);
}
