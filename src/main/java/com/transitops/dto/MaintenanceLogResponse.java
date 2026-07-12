// dto/MaintenanceLogResponse.java
package com.transitops.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class MaintenanceLogResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleName;
    private String registrationNumber;
    private String description;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal cost;
    private String status;
    private String notes;
}