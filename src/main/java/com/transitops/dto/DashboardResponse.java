package com.transitops.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private long activeVehicles;
    private long availableVehicles;
    private long vehiclesInMaintenance;
    private long retiredVehicles;
    private long activeTrips;
    private long pendingTrips;
    private long driversOnDuty;
    private long availableDrivers;
    private BigDecimal fleetUtilization;
    private List<VehicleStatusCount> vehicleStatusCounts;
    private List<TripStatusCount> tripStatusCounts;

    // --- ADD THESE TWO NEW FIELDS ---
    private List<ActiveTripDto> activeTripsList;
    private List<String> notifications;

    @Data
    @Builder
    public static class VehicleStatusCount {
        private String status;
        private long count;
    }

    @Data
    @Builder
    public static class TripStatusCount {
        private String status;
        private long count;
    }

    // --- ADD THIS INNER CLASS ---
    @Data
    @Builder
    public static class ActiveTripDto {
        private String tripNumber;
        private String vehicleReg;
        private String driverName;
        private String route;
    }
}