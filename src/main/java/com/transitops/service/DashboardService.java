package com.transitops.service;

import com.transitops.dto.DashboardResponse;
import com.transitops.entity.Driver;
import com.transitops.entity.Trip;
import com.transitops.entity.enums.DriverStatus;
import com.transitops.entity.enums.TripStatus;
import com.transitops.entity.enums.VehicleStatus;
import com.transitops.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;

    public DashboardResponse getDashboardStats() {
        long totalVehicles = vehicleRepository.count();
        long activeVehicles = vehicleRepository.countByStatus(VehicleStatus.ON_TRIP);
        long availableVehicles = vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
        long vehiclesInMaintenance = vehicleRepository.countByStatus(VehicleStatus.IN_SHOP);
        long retiredVehicles = vehicleRepository.countByStatus(VehicleStatus.RETIRED);

        long activeTrips = tripRepository.countByStatus(TripStatus.DISPATCHED);
        long pendingTrips = tripRepository.countByStatus(TripStatus.DRAFT);

        long driversOnDuty = driverRepository.countByStatus(DriverStatus.ON_TRIP);
        long availableDrivers = driverRepository.countByStatus(DriverStatus.AVAILABLE);

        BigDecimal fleetUtilization = BigDecimal.ZERO;
        long operationalVehicles = totalVehicles - retiredVehicles;
        if (operationalVehicles > 0) {
            fleetUtilization = BigDecimal.valueOf(activeVehicles)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(operationalVehicles), 2, RoundingMode.HALF_UP);
        }

        List<DashboardResponse.VehicleStatusCount> vehicleStatusCounts = Arrays.stream(VehicleStatus.values())
                .map(status -> DashboardResponse.VehicleStatusCount.builder()
                        .status(status.name())
                        .count(vehicleRepository.countByStatus(status))
                        .build())
                .collect(Collectors.toList());

        List<DashboardResponse.TripStatusCount> tripStatusCounts = Arrays.stream(TripStatus.values())
                .map(status -> DashboardResponse.TripStatusCount.builder()
                        .status(status.name())
                        .count(tripRepository.countByStatus(status))
                        .build())
                .collect(Collectors.toList());

        // --- NEW: Fetch Active Trips for Dashboard Table ---
        List<DashboardResponse.ActiveTripDto> activeTripsList = tripRepository.findByStatus(TripStatus.DISPATCHED)
                .stream()
                .map(t -> DashboardResponse.ActiveTripDto.builder()
                        .tripNumber(t.getTripNumber())
                        .vehicleReg(t.getVehicle() != null ? t.getVehicle().getRegistrationNumber() : "N/A")
                        .driverName(t.getDriver() != null ? t.getDriver().getName() : "N/A")
                        .route(t.getSource() + " ➜ " + t.getDestination())
                        .build())
                .collect(Collectors.toList());

        // --- NEW: Generate Notifications ---
        List<String> notifications = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate warningThreshold = today.plusDays(30); // Warn if expiring within 30 days

        List<Driver> allDrivers = driverRepository.findAll();
        for (Driver d : allDrivers) {
            if (d.getLicenseExpiry() != null) {
                if (d.getLicenseExpiry().isBefore(today) && d.getStatus() == DriverStatus.SUSPENDED) {
                    notifications.add("⚠️ <strong>" + d.getName() + "</strong> is suspended (License expired on " + d.getLicenseExpiry() + "). Update license to reactivate.");
                } else if (d.getLicenseExpiry().isBefore(warningThreshold) && d.getLicenseExpiry().isAfter(today) && d.getStatus() != DriverStatus.SUSPENDED) {
                    notifications.add("🔔 <strong>" + d.getName() + "</strong>'s license expires soon on " + d.getLicenseExpiry() + "!");
                }
            }
        }

        return DashboardResponse.builder()
                .activeVehicles(activeVehicles)
                .availableVehicles(availableVehicles)
                .vehiclesInMaintenance(vehiclesInMaintenance)
                .retiredVehicles(retiredVehicles)
                .activeTrips(activeTrips)
                .pendingTrips(pendingTrips)
                .driversOnDuty(driversOnDuty)
                .availableDrivers(availableDrivers)
                .fleetUtilization(fleetUtilization)
                .vehicleStatusCounts(vehicleStatusCounts)
                .tripStatusCounts(tripStatusCounts)
                .activeTripsList(activeTripsList) // Attach to response
                .notifications(notifications)       // Attach to response
                .build();
    }
}