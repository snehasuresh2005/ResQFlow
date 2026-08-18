package com.resqflow.domain.request;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "resource_requirements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    @JsonBackReference
    private EmergencyRequest request;

    @NotBlank
    @Column(name = "resource_type", nullable = false)
    private String resourceType; // FOOD, WATER, MEDICAL, SHELTER, EQUIPMENT

    @NotNull
    @Column(nullable = false)
    private Double quantity;

    @NotBlank
    @Column(nullable = false)
    private String unit;
}
