package com.resqflow.domain.resource;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("FOOD")
@Getter
@Setter
public class FoodResource extends Resource {

    @Column(name = "storage_requirement")
    private String storageRequirement; // REFRIGERATED, DRY, FROZEN
}
