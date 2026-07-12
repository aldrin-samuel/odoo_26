// controller/ExpenseController.java
package com.transitops.controller;

import com.transitops.dto.ExpenseRequest;
import com.transitops.dto.ExpenseResponse;
import com.transitops.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(@RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.addExpense(request));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(expenseService.getExpensesByVehicle(vehicleId));
    }
}