package com.transitops.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseDto {
    private Long id;
    private String vehicleName;
    private String type;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
}