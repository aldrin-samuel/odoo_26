// dto/FuelLogResponse.java
package com.transitops.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FuelLogResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleName;
    private String registrationNumber;
    private Long tripId;
    private String tripNumber;
    private BigDecimal liters;
    private BigDecimal costPerLiter;
    private BigDecimal totalCost;
    private LocalDate date;
    private BigDecimal odometerReading;
    private String fuelStation;
    private String driverName;
}