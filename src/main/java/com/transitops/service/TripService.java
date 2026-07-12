package com.transitops.service;

import com.transitops.entity.Driver;
import com.transitops.entity.Trip;
import com.transitops.entity.Vehicle;
import com.transitops.entity.enums.DriverStatus;
import com.transitops.entity.enums.TripStatus;
import com.transitops.entity.enums.VehicleStatus;
import com.transitops.repository.DriverRepository;
import com.transitops.repository.TripRepository;
import com.transitops.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public TripService(TripRepository tripRepository, VehicleRepository vehicleRepository, DriverRepository driverRepository) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    public List<Trip> getTrips() {
        return tripRepository.findAll();
    }

    @Transactional
    public Trip createTrip(Trip trip) {
        Vehicle vehicle = vehicleRepository.findById(trip.getVehicle().getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        Driver driver = driverRepository.findById(trip.getDriver().getId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new RuntimeException("Vehicle is not available (Status: " + vehicle.getStatus() + ")");
        }
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new RuntimeException("Driver is not available (Status: " + driver.getStatus() + ")");
        }
        if (driver.getLicenseExpiry().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot assign driver: License expired on " + driver.getLicenseExpiry());
        }

        // SAFETY CHECK: Prevent crash if frontend sends null
        if (trip.getCargoWeight() == null) {
            throw new RuntimeException("Cargo weight is required!");
        }
        if (trip.getCargoWeight().doubleValue() > vehicle.getMaxLoadCapacity()) {
            throw new RuntimeException("Cargo weight (" + trip.getCargoWeight() + " kg) exceeds vehicle max capacity (" + vehicle.getMaxLoadCapacity() + " kg)");
        }

        if (trip.getTripNumber() == null || trip.getTripNumber().isEmpty()) {
            trip.setTripNumber("TRP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        vehicle.setStatus(VehicleStatus.ON_TRIP);
        driver.setStatus(DriverStatus.ON_TRIP);
        vehicleRepository.save(vehicle);
        driverRepository.save(driver);

        trip.setStatus(TripStatus.DISPATCHED);
        return tripRepository.save(trip);
    }

    // UPDATED METHOD to accept JSON body from frontend
    @Transactional
    public void completeTrip(Long id, Map<String, Object> body) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getStatus() != TripStatus.DISPATCHED) {
            throw new RuntimeException("Only dispatched trips can be completed");
        }

        // Safely extract values from the JSON map
        if (body.containsKey("actualDistance")) {
            trip.setActualDistance(new BigDecimal(body.get("actualDistance").toString()));
        }
        if (body.containsKey("fuelConsumed")) {
            trip.setFuelConsumed(new BigDecimal(body.get("fuelConsumed").toString()));
        }
        if (body.containsKey("endOdometer")) {
            trip.setEndOdometer(new BigDecimal(body.get("endOdometer").toString()));
        }

        Vehicle vehicle = trip.getVehicle();
        Driver driver = trip.getDriver();

        vehicle.setStatus(VehicleStatus.AVAILABLE);
        driver.setStatus(DriverStatus.AVAILABLE);
        vehicleRepository.save(vehicle);
        driverRepository.save(driver);

        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletionTime(LocalDateTime.now());
        tripRepository.save(trip);
    }

    @Transactional
    public void cancelTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getStatus() != TripStatus.DRAFT && trip.getStatus() != TripStatus.DISPATCHED) {
            throw new RuntimeException("Cannot cancel this trip");
        }

        if (trip.getStatus() == TripStatus.DISPATCHED) {
            Vehicle vehicle = trip.getVehicle();
            Driver driver = trip.getDriver();
            if (vehicle.getStatus() == VehicleStatus.ON_TRIP) {
                vehicle.setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(vehicle);
            }
            if (driver.getStatus() == DriverStatus.ON_TRIP) {
                driver.setStatus(DriverStatus.AVAILABLE);
                driverRepository.save(driver);
            }
        }
        trip.setStatus(TripStatus.CANCELLED);
        tripRepository.save(trip);
    }
}