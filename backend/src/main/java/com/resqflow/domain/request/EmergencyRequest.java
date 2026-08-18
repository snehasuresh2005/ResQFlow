package com.resqflow.domain.request;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.resqflow.domain.location.EmergencyZone;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emergency_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "request_number", unique = true, nullable = false)
    private String requestNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "emergency_zone_id", nullable = false)
    private EmergencyZone emergencyZone;

    @NotBlank
    @Column(name = "request_type", nullable = false)
    private String requestType; // FOOD, WATER, MEDICAL, SHELTER, EVACUATION, EQUIPMENT

    @NotBlank
    @Column(nullable = false)
    private String priority; // CRITICAL, HIGH, MEDIUM, LOW

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    private List<ResourceRequirement> requestedResources = new ArrayList<>();

    @NotNull
    @Column(name = "number_affected", nullable = false)
    private Integer numberOfPeopleAffected;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime deadline;

    @NotBlank
    @Column(nullable = false)
    private String status; // CREATED, UNDER_EVALUATION, PARTIALLY_ALLOCATED, FULLY_ALLOCATED, IN_PROGRESS, FULFILLED, CANCELLED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
