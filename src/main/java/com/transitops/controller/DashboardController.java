package com.transitops.controller;

import com.transitops.repository.DriverRepository;
import com.transitops.repository.TripRepository;
import com.transitops.repository.VehicleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;

    public DashboardController(VehicleRepository vehicleRepository,
                               DriverRepository driverRepository,
                               TripRepository tripRepository) {

        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.tripRepository = tripRepository;
    }

    @GetMapping
    public Map<String, Long> getDashboard() {

        Map<String, Long> dashboard = new HashMap<>();

        dashboard.put("totalVehicles", vehicleRepository.count());

        dashboard.put("totalDrivers", driverRepository.count());

        dashboard.put("totalTrips", tripRepository.count());

        return dashboard;

    }

}