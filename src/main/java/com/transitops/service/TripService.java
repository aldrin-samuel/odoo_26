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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public TripService(TripRepository tripRepository,
                       VehicleRepository vehicleRepository,
                       DriverRepository driverRepository) {
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

        // Business Rule: Retired or In Shop vehicles must never appear in dispatch
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new RuntimeException("Vehicle is not available (Status: " + vehicle.getStatus() + ")");
        }

        // Business Rule: Suspended drivers or drivers already On Trip cannot be assigned
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new RuntimeException("Driver is not available (Status: " + driver.getStatus() + ")");
        }

        // Business Rule: Drivers with expired licenses cannot be assigned to trips
        if (driver.getLicenseExpiryDate() != null && driver.getLicenseExpiryDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot assign driver: License expired on " + driver.getLicenseExpiryDate());
        }

        // Business Rule: Cargo Weight must not exceed the vehicle's maximum load capacity
        if (trip.getCargoWeight().doubleValue() > vehicle.getMaxLoadCapacity()) {
            throw new RuntimeException("Cargo weight (" + trip.getCargoWeight() + " kg) exceeds vehicle max capacity (" + vehicle.getMaxLoadCapacity() + " kg)");
        }

        // Generate unique trip number if not provided
        if (trip.getTripNumber() == null || trip.getTripNumber().isEmpty()) {
            trip.setTripNumber("TRP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Dispatch
        vehicle.setStatus(VehicleStatus.ON_TRIP);
        driver.setStatus(DriverStatus.ON_TRIP);

        vehicleRepository.save(vehicle);
        driverRepository.save(driver);

        trip.setStatus(TripStatus.DISPATCHED);

        return tripRepository.save(trip);
    }

    @Transactional
    public void completeTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getStatus() != TripStatus.DISPATCHED) {
            throw new RuntimeException("Only dispatched trips can be completed");
        }

        Vehicle vehicle = trip.getVehicle();
        Driver driver = trip.getDriver();

        // Business Rule: Completing a trip automatically changes both to Available
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        driver.setStatus(DriverStatus.AVAILABLE);

        vehicleRepository.save(vehicle);
        driverRepository.save(driver);

        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletionTime(java.time.LocalDateTime.now());

        tripRepository.save(trip);
    }

    @Transactional
    public void cancelTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Business Rule: Only Draft or Dispatched trips can be cancelled
        if (trip.getStatus() != TripStatus.DRAFT && trip.getStatus() != TripStatus.DISPATCHED) {
            throw new RuntimeException("Cannot cancel a trip that is already completed or cancelled");
        }

        Vehicle vehicle = trip.getVehicle();
        Driver driver = trip.getDriver();

        // Business Rule: Cancelling a dispatched trip restores the vehicle and driver to Available
        if (trip.getStatus() == TripStatus.DISPATCHED) {
            if (vehicle != null && vehicle.getStatus() == VehicleStatus.ON_TRIP) {
                vehicle.setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(vehicle);
            }

            if (driver != null && driver.getStatus() == DriverStatus.ON_TRIP) {
                driver.setStatus(DriverStatus.AVAILABLE);
                driverRepository.save(driver);
            }
        }

        trip.setStatus(TripStatus.CANCELLED);
        tripRepository.save(trip);
    }
}