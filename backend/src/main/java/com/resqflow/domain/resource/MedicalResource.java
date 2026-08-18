package com.resqflow.domain.resource;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("MEDICAL")
@Getter
@Setter
public class MedicalResource extends Resource {

    @Column(name = "temp_constraint")
    private Double tempConstraint; // Celsius limit, e.g., 4.0
}
