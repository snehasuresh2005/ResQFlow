package com.resqflow.domain.resource;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SHELTER")
public class ShelterKitResource extends Resource {
}
