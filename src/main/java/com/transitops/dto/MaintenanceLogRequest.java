// dto/MaintenanceLogRequest.java
package com.transitops.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MaintenanceLogRequest {
    private Long vehicleId;
    private String description;
    private String type;
    private LocalDate startDate;
    private BigDecimal cost;
    private String notes;
}