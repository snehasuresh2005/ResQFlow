package com.resqflow.domain.resource;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("WATER")
public class WaterResource extends Resource {
}
