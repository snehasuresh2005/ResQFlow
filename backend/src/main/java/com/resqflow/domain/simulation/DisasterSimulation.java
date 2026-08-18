package com.resqflow.domain.simulation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "disaster_simulations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisasterSimulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String scenario; // FLOOD, EARTHQUAKE, WILDFIRE

    @NotNull
    @Column(name = "requests_processed", nullable = false)
    private Integer requestsProcessed;

    @NotNull
    @Column(name = "requests_fulfilled", nullable = false)
    private Integer requestsFulfilled;

    @NotNull
    @Column(name = "critical_fulfillment_rate", nullable = false)
    private Double criticalFulfillmentRate;

    @NotNull
    @Column(name = "avg_response_time", nullable = false)
    private Double avgResponseTime; // in minutes

    @NotNull
    @Column(name = "vehicle_utilization", nullable = false)
    private Double vehicleUtilization; // ratio, e.g. 0.85

    @NotNull
    @Column(name = "resource_wastage", nullable = false)
    private Double resourceWastage; // ratio, e.g. 0.05

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
