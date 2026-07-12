// entity/MaintenanceLog.java
package com.transitops.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String type; // Oil Change, Tire Rotation, Repair, etc.

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal cost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.ACTIVE;

    private String notes;

    public enum MaintenanceStatus {
        ACTIVE, COMPLETED
    }
}