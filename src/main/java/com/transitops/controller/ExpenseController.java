package com.transitops.controller;

import com.transitops.dto.ExpenseDto;
import com.transitops.dto.ExpenseRequest;
import com.transitops.dto.ExpenseResponse;
import com.transitops.entity.Expense;
import com.transitops.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // Convert Entity to simple DTO so JSON doesn't crash
    @GetMapping
    public List<ExpenseDto> getAllExpenses() {
        return expenseService.getAllExpensesRaw().stream().map(e -> {
            ExpenseDto dto = new ExpenseDto();
            dto.setId(e.getId());
            dto.setVehicleName(e.getVehicle() != null ? e.getVehicle().getVehicleName() : "General");
            dto.setType(e.getType().name());
            dto.setDescription(e.getDescription());
            dto.setAmount(e.getAmount());
            dto.setDate(e.getDate());
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> addExpense(@RequestBody ExpenseRequest request) {
        try {
            request.setType(request.getType().toUpperCase());
            return ResponseEntity.ok(expenseService.addExpense(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}