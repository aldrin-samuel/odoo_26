// controller/MaintenanceController.java
package com.transitops.controller;

import com.transitops.dto.MaintenanceLogRequest;
import com.transitops.dto.MaintenanceLogResponse;
import com.transitops.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping
    public ResponseEntity<MaintenanceLogResponse> createMaintenance(@RequestBody MaintenanceLogRequest request) {
        return ResponseEntity.ok(maintenanceService.createMaintenance(request));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<MaintenanceLogResponse> completeMaintenance(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceService.completeMaintenance(id));
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceLogResponse>> getAllMaintenance() {
        return ResponseEntity.ok(maintenanceService.getAllMaintenance());
    }

    @GetMapping("/vehicle/{vehicleId}/active")
    public ResponseEntity<List<MaintenanceLogResponse>> getActiveMaintenanceByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(maintenanceService.getActiveMaintenanceByVehicle(vehicleId));
    }
}