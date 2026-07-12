package com.transitops.entity;

import com.transitops.entity.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private String vehicleName;

    @Column(nullable = false)
    private String vehicleType;

    @Column(nullable = false)
    private Double maxLoadCapacity;

    private Double odometer;

    private Double acquisitionCost;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status = VehicleStatus.AVAILABLE;
}