// entity/FuelLog.java
package com.transitops.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fuel_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(nullable = false)
    private BigDecimal liters;

    @Column(nullable = false)
    private BigDecimal costPerLiter;

    @Column(nullable = false)
    private BigDecimal totalCost;

    @Column(nullable = false)
    private LocalDate date;

    private BigDecimal odometerReading;
    private String fuelStation;
    private String notes;
}