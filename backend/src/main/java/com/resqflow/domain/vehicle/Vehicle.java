package com.resqflow.domain.vehicle;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicle_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "vehicle_type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Truck.class, name = "TRUCK"),
    @JsonSubTypes.Type(value = Van.class, name = "VAN"),
    @JsonSubTypes.Type(value = Ambulance.class, name = "AMBULANCE"),
    @JsonSubTypes.Type(value = Boat.class, name = "BOAT")
})
public abstract class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "registration_number", unique = true, nullable = false)
    private String registrationNumber;

    @NotNull
    @Column(name = "capacity_weight", nullable = false)
    private Double capacityWeight;

    @NotNull
    @Column(name = "capacity_volume", nullable = false)
    private Double capacityVolume;

    @NotNull
    @Column(name = "fuel_level", nullable = false)
    private Double fuelLevel;

    @NotNull
    @Column(name = "current_latitude", nullable = false)
    private Double currentLatitude;

    @NotNull
    @Column(name = "current_longitude", nullable = false)
    private Double currentLongitude;

    @NotBlank
    @Column(nullable = false)
    private String status; // AVAILABLE, ASSIGNED, IN_TRANSIT, MAINTENANCE, OUT_OF_SERVICE

    @Column(name = "vehicle_type", insertable = false, updatable = false)
    private String vehicleType;

    @Version
    private Long version;

    public boolean canCarry(Double weight, Double volume) {
        return weight <= capacityWeight && volume <= capacityVolume;
    }

    public abstract boolean supportsRouteType(String routeType);
}
