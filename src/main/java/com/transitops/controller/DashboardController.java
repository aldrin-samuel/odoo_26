package com.transitops.controller;

import com.transitops.service.VehicleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DashboardController {

    private final VehicleService vehicleService;

    public DashboardController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/api/dashboard")
    public Map<String, Long> dashboard() {

        return Map.of(
                "totalVehicles",
                vehicleService.countVehicles()
        );

    }

}