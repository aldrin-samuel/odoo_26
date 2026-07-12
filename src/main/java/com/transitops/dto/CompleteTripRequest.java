// dto/CompleteTripRequest.java
package com.transitops.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CompleteTripRequest {
    private BigDecimal actualDistance;
    private BigDecimal fuelConsumed;
    private BigDecimal endOdometer;
}