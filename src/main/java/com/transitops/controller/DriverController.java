package com.transitops.controller;

import com.transitops.entity.Driver; // ENSURE THIS IS YOUR ENTITY, NOT java.sql.Driver
import com.transitops.service.DriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public ResponseEntity<List<Driver>> getAll() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @PostMapping
    public ResponseEntity<?> addDriver(@RequestBody Driver driver) {
        try {
            Map<String, Object> result = driverService.addDriver(driver);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<?> suspendDriver(@PathVariable Long id) {
        try {
            driverService.suspendDriver(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/license")
    public ResponseEntity<?> updateLicense(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            driverService.updateLicenseExpiry(id, body.get("newExpiryDate"));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/off-duty")
    public ResponseEntity<?> setOffDuty(@PathVariable Long id) {
        try {
            driverService.setOffDuty(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/available")
    public ResponseEntity<?> setAvailable(@PathVariable Long id) {
        try {
            driverService.setAvailable(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivateDriver(@PathVariable Long id) {
        try {
            driverService.reactivateDriver(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}