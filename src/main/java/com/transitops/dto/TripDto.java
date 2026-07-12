package com.transitops.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TripDto {
    private Long id;
    private String tripNumber;
    private String vehicleRegNumber;
    private String driverName;
    private String source;
    private String destination;
    private BigDecimal cargoWeight;
    private String status;
}