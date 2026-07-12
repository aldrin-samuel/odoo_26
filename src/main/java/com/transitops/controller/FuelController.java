// controller/FuelController.java
package com.transitops.controller;

import com.transitops.dto.FuelLogRequest;
import com.transitops.dto.FuelLogResponse;
import com.transitops.service.FuelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fuel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FuelController {

    private final FuelService fuelService;

    @PostMapping
    public ResponseEntity<FuelLogResponse> addFuelLog(@RequestBody FuelLogRequest request) {
        return ResponseEntity.ok(fuelService.addFuelLog(request));
    }

    @GetMapping
    public ResponseEntity<List<FuelLogResponse>> getAllFuelLogs() {
        return ResponseEntity.ok(fuelService.getAllFuelLogs());
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<FuelLogResponse>> getFuelLogsByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(fuelService.getFuelLogsByVehicle(vehicleId));
    }
}