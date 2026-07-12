// entity/Trip.java
package com.transitops.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip extends BaseEntity {

    @Column(nullable = false)
    private String tripNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private BigDecimal cargoWeight;

    @Column(nullable = false)
    private BigDecimal plannedDistance;

    private BigDecimal actualDistance;

    private BigDecimal fuelConsumed;

    private BigDecimal startOdometer;
    private BigDecimal endOdometer;

    private LocalDateTime dispatchTime;
    private LocalDateTime completionTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TripStatus status = TripStatus.DRAFT;

    private String notes;

    public enum TripStatus {
        DRAFT, DISPATCHED, COMPLETED, CANCELLED
    }
}