// dto/FuelLogRequest.java
package com.transitops.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FuelLogRequest {
    private Long vehicleId;
    private Long tripId;
    private BigDecimal liters;
    private BigDecimal costPerLiter;
    private LocalDate date;
    private BigDecimal odometerReading;
    private String fuelStation;
    private String notes;
}