package com.transitops.service;

import com.transitops.entity.Vehicle;
import com.transitops.entity.enums.TripStatus; // CORRECT IMPORT
import com.transitops.entity.enums.VehicleStatus;
import com.transitops.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final FuelLogRepository fuelLogRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final ExpenseRepository expenseRepository;

    public Map<String, Object> getVehicleAnalytics(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        BigDecimal totalFuelCost = fuelLogRepository.getTotalFuelCostByVehicle(vehicleId);
        BigDecimal totalMaintenanceCost = maintenanceLogRepository.getTotalMaintenanceCostByVehicle(vehicleId);
        BigDecimal totalOtherExpenses = expenseRepository.getTotalExpensesByVehicle(vehicleId);
        BigDecimal totalOperationalCost = totalFuelCost.add(totalMaintenanceCost).add(totalOtherExpenses);

        BigDecimal fuelEfficiency = calculateFuelEfficiency(vehicleId);

        BigDecimal revenue = calculateRevenue(vehicleId);
        Double acquisitionCostDouble = vehicle.getAcquisitionCost() != null ? vehicle.getAcquisitionCost() : 1.0;
        BigDecimal acquisitionCost = BigDecimal.valueOf(acquisitionCostDouble);

        BigDecimal roi = revenue.subtract(totalMaintenanceCost.add(totalFuelCost))
                .divide(acquisitionCost, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("vehicleId", vehicleId);
        analytics.put("vehicleName", vehicle.getVehicleName());
        analytics.put("registrationNumber", vehicle.getRegistrationNumber());
        analytics.put("totalFuelCost", totalFuelCost);
        analytics.put("totalMaintenanceCost", totalMaintenanceCost);
        analytics.put("totalOtherExpenses", totalOtherExpenses);
        analytics.put("totalOperationalCost", totalOperationalCost);
        analytics.put("fuelEfficiency", fuelEfficiency);
        analytics.put("revenue", revenue);
        analytics.put("roi", roi);

        return analytics;
    }

    public List<Map<String, Object>> getAllVehiclesAnalytics() {
        return vehicleRepository.findAll().stream()
                .map(vehicle -> getVehicleAnalytics(vehicle.getId()))
                .collect(Collectors.toList());
    }

    private BigDecimal calculateFuelEfficiency(Long vehicleId) {
        BigDecimal totalFuel = fuelLogRepository.getTotalFuelLitersByVehicle(vehicleId);
        if (totalFuel.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalDistance = tripRepository.findAll().stream()
                .filter(t -> t.getVehicle() != null && t.getVehicle().getId().equals(vehicleId))
                .filter(t -> t.getStatus() == TripStatus.COMPLETED) // FIXED: Removed com.transitops.entity.Trip.
                .map(t -> t.getActualDistance() != null ? t.getActualDistance() : t.getPlannedDistance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalDistance.divide(totalFuel, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRevenue(Long vehicleId) {
        BigDecimal ratePerKm = new BigDecimal("10");
        BigDecimal totalDistance = tripRepository.findAll().stream()
                .filter(t -> t.getVehicle() != null && t.getVehicle().getId().equals(vehicleId))
                .filter(t -> t.getStatus() == TripStatus.COMPLETED) // FIXED: Removed com.transitops.entity.Trip.
                .map(t -> t.getActualDistance() != null ? t.getActualDistance() : t.getPlannedDistance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalDistance.multiply(ratePerKm);
    }
}