package com.resqflow.domain.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.resqflow.domain.location.Depot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "resources")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "resource_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "resource_type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = FoodResource.class, name = "FOOD"),
    @JsonSubTypes.Type(value = WaterResource.class, name = "WATER"),
    @JsonSubTypes.Type(value = MedicalResource.class, name = "MEDICAL"),
    @JsonSubTypes.Type(value = ShelterKitResource.class, name = "SHELTER"),
    @JsonSubTypes.Type(value = EquipmentResource.class, name = "EQUIPMENT")
})
public abstract class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(name = "resource_type", insertable = false, updatable = false)
    private String resourceType;

    @NotNull
    @Column(nullable = false)
    private Double quantity;

    @NotBlank
    @Column(nullable = false)
    private String unit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Integer priority = 0;

    @NotNull
    @Column(name = "weight_per_unit", nullable = false)
    private Double weightPerUnit;

    @NotNull
    @Column(name = "volume_per_unit", nullable = false)
    private Double volumePerUnit;

    @NotBlank
    @Column(nullable = false)
    private String status; // AVAILABLE, OUT_OF_STOCK, EXPIRED

    @Version
    private Long version;

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
}
