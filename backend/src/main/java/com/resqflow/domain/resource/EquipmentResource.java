package com.resqflow.domain.resource;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("EQUIPMENT")
@Getter
@Setter
public class EquipmentResource extends Resource {

    @Column(name = "is_reusable")
    private Boolean isReusable = false;
}
