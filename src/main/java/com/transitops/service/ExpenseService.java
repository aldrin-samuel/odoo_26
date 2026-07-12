// service/ExpenseService.java
package com.transitops.service;

import com.transitops.dto.ExpenseRequest;
import com.transitops.dto.ExpenseResponse;
import com.transitops.entity.Expense;
import com.transitops.entity.Trip;
import com.transitops.entity.Vehicle;
import com.transitops.repository.ExpenseRepository;
import com.transitops.repository.TripRepository;
import com.transitops.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;

    public ExpenseResponse addExpense(ExpenseRequest request) {
        Vehicle vehicle = null;
        if (request.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        }

        Trip trip = null;
        if (request.getTripId() != null) {
            trip = tripRepository.findById(request.getTripId())
                    .orElseThrow(() -> new RuntimeException("Trip not found"));
        }

        Expense expense = Expense.builder()
                .vehicle(vehicle)
                .trip(trip)
                .description(request.getDescription())
                .type(Expense.ExpenseType.valueOf(request.getType()))
                .amount(request.getAmount())
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .notes(request.getNotes())
                .build();

        expense = expenseRepository.save(expense);
        return toResponse(expense);
    }

    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getExpensesByVehicle(Long vehicleId) {
        return expenseRepository.findByVehicleId(vehicleId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .vehicleId(expense.getVehicle() != null ? expense.getVehicle().getId() : null)
                .vehicleName(expense.getVehicle() != null ? expense.getVehicle().getVehicleName() : null)  // CORRECT METHOD
                .tripId(expense.getTrip() != null ? expense.getTrip().getId() : null)
                .tripNumber(expense.getTrip() != null ? expense.getTrip().getTripNumber() : null)
                .description(expense.getDescription())
                .type(expense.getType().name())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .build();
    }
}