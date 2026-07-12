// dto/ExpenseResponse.java
package com.transitops.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ExpenseResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleName;
    private Long tripId;
    private String tripNumber;
    private String description;
    private String type;
    private BigDecimal amount;
    private LocalDate date;
    private String driverName;
}