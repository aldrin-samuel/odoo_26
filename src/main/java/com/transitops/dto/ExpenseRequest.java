// dto/ExpenseRequest.java
package com.transitops.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {
    private Long vehicleId;
    private Long tripId;
    private String description;
    private String type;
    private BigDecimal amount;
    private LocalDate date;
    private String notes;
}