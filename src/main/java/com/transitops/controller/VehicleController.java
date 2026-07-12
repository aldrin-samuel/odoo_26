package com.transitops.controller;

import com.transitops.entity.Vehicle;
import com.transitops.entity.enums.VehicleStatus;
import com.transitops.repository.VehicleRepository;
import com.transitops.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;

    public VehicleController(VehicleRepository vehicleRepository, VehicleService vehicleService) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    @PostMapping
    public ResponseEntity<?> addVehicle(@RequestBody Vehicle vehicle) {
        try {
            return ResponseEntity.ok(vehicleService.addVehicle(vehicle));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok().build();
    }

    // FEATURE: Retire Vehicle Endpoint
    @PutMapping("/{id}/retire")
    public ResponseEntity<?> retireVehicle(@PathVariable Long id) {
        try {
            Vehicle vehicle = vehicleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));

            if (vehicle.getStatus() == VehicleStatus.ON_TRIP) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot retire a vehicle that is on a trip!"));
            }

            vehicle.setStatus(VehicleStatus.RETIRED);
            vehicleRepository.save(vehicle);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}