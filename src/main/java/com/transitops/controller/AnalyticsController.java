// controller/AnalyticsController.java
package com.transitops.controller;

import com.transitops.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<Map<String, Object>> getVehicleAnalytics(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(analyticsService.getVehicleAnalytics(vehicleId));
    }

    @GetMapping("/fleet")
    public ResponseEntity<List<Map<String, Object>>> getFleetAnalytics() {
        return ResponseEntity.ok(analyticsService.getAllVehiclesAnalytics());
    }

    @GetMapping("/fleet/csv")
    public ResponseEntity<byte[]> exportFleetAnalyticsCsv() {
        List<Map<String, Object>> analytics = analyticsService.getAllVehiclesAnalytics();

        StringBuilder csv = new StringBuilder();
        csv.append("Vehicle,Registration,Fuel Cost,Maintenance Cost,Other Expenses,Total Cost,Fuel Efficiency (km/L),ROI (%)\n");

        for (Map<String, Object> data : analytics) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s%n",
                    data.get("vehicleName"),
                    data.get("registrationNumber"),
                    data.get("totalFuelCost"),
                    data.get("totalMaintenanceCost"),
                    data.get("totalOtherExpenses"),
                    data.get("totalOperationalCost"),
                    data.get("fuelEfficiency"),
                    data.get("roi")
            ));
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=fleet-analytics.csv")
                .header("Content-Type", "text/csv")
                .body(csv.toString().getBytes());
    }
}