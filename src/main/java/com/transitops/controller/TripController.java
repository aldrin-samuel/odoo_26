package com.transitops.controller;

import com.transitops.dto.TripDto;
import com.transitops.entity.Trip;
import com.transitops.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    // Convert Entity to simple DTO so JSON doesn't crash
    @GetMapping
    public List<TripDto> getTrips() {
        return tripService.getTrips().stream().map(t -> {
            TripDto dto = new TripDto();
            dto.setId(t.getId());
            dto.setTripNumber(t.getTripNumber());
            dto.setVehicleRegNumber(t.getVehicle() != null ? t.getVehicle().getRegistrationNumber() : "N/A");
            dto.setDriverName(t.getDriver() != null ? t.getDriver().getName() : "N/A");
            dto.setSource(t.getSource());
            dto.setDestination(t.getDestination());
            dto.setCargoWeight(t.getCargoWeight());
            dto.setStatus(t.getStatus().name());
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> createTrip(@RequestBody Trip trip) {
        try {
            return ResponseEntity.ok(tripService.createTrip(trip));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeTrip(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            tripService.completeTrip(id, body);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelTrip(@PathVariable Long id) {
        tripService.cancelTrip(id);
        return ResponseEntity.ok().build();
    }
}