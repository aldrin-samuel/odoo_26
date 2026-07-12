package com.transitops.service;

import com.transitops.dto.DashboardResponse;
import com.transitops.entity.enums.VehicleStatus;  // From enums folder
import com.transitops.entity.enums.DriverStatus;   // From enums folder
import com.transitops.entity.enums.TripStatus;     // From enums folder
import com.transitops.repository.DriverRepository;
import com.transitops.repository.TripRepository;
import com.transitops.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// ... rest of the DashboardService code using VehicleStatus, DriverStatus, TripStatus directly

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

        // Check if you have DriverStatus enum or if it's defined differently
        long driversOnDuty = 0;
        long availableDrivers = 0;

        try {
            driversOnDuty = driverRepository.countByStatus(DriverStatus.ON_TRIP);
            availableDrivers = driverRepository.countByStatus(DriverStatus.AVAILABLE);
        } catch (Exception e) {
            // Fallback if DriverStatus is structured differently
            System.out.println("Driver status count not available: " + e.getMessage());
        }

        // Fleet Utilization = Active Vehicles / (Total - Retired) * 100
        BigDecimal fleetUtilization = BigDecimal.ZERO;
        long operationalVehicles = totalVehicles - retiredVehicles;
        if (operationalVehicles > 0) {
            fleetUtilization = BigDecimal.valueOf(activeVehicles)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(operationalVehicles), 2, RoundingMode.HALF_UP);
        }

        // Vehicle status counts
        List<DashboardResponse.VehicleStatusCount> vehicleStatusCounts = Arrays.stream(VehicleStatus.values())
                .map(status -> DashboardResponse.VehicleStatusCount.builder()
                        .status(status.name())
                        .count(vehicleRepository.countByStatus(status))
                        .build())
                .collect(Collectors.toList());

        // Trip status counts
        List<DashboardResponse.TripStatusCount> tripStatusCounts = Arrays.stream(TripStatus.values())
                .map(status -> DashboardResponse.TripStatusCount.builder()
                        .status(status.name())
                        .count(tripRepository.countByStatus(status))
                        .build())
                .collect(Collectors.toList());

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
                .build();
    }
}