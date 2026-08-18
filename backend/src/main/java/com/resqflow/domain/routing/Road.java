package com.resqflow.domain.routing;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "roads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Road {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(name = "start_node_type", nullable = false)
    private String startNodeType; // DEPOT, ZONE, SHELTER, INTERSECTION

    @NotNull
    @Column(name = "start_node_id", nullable = false)
    private Long startNodeId;

    @NotBlank
    @Column(name = "end_node_type", nullable = false)
    private String endNodeType; // DEPOT, ZONE, SHELTER, INTERSECTION

    @NotNull
    @Column(name = "end_node_id", nullable = false)
    private Long endNodeId;

    @NotNull
    @Column(nullable = false)
    private Double distance; // in km

    @NotNull
    @Column(name = "travel_time", nullable = false)
    private Double travelTime; // in minutes

    @NotBlank
    @Column(nullable = false)
    private String status; // OPEN, BLOCKED, RESTRICTED
}
