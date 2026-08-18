package com.resqflow.domain.routing;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "route_nodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    @JsonBackReference
    private Route route;

    @NotNull
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @NotBlank
    @Column(name = "node_type", nullable = false)
    private String nodeType; // DEPOT, ZONE, SHELTER, INTERSECTION

    @NotNull
    @Column(name = "node_id", nullable = false)
    private Long nodeId;
}
