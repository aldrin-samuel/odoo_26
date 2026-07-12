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

import java.util.List;

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

    public Trip createTrip(Trip trip) {

        Vehicle vehicle = vehicleRepository.findById(
                trip.getVehicle().getId()
        ).orElseThrow();

        Driver driver = driverRepository.findById(
                trip.getDriver().getId()
        ).orElseThrow();

        // Business Rule 1
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new RuntimeException("Vehicle not available");
        }

        // Business Rule 2
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new RuntimeException("Driver not available");
        }

        // Dispatch
        vehicle.setStatus(VehicleStatus.ON_TRIP);
        driver.setStatus(DriverStatus.ON_TRIP);

        vehicleRepository.save(vehicle);
        driverRepository.save(driver);

        trip.setStatus(TripStatus.DISPATCHED);

        return tripRepository.save(trip);
    }

    public void completeTrip(Long id) {

        Trip trip = tripRepository.findById(id).orElseThrow();

        Vehicle vehicle = trip.getVehicle();
        Driver driver = trip.getDriver();

        vehicle.setStatus(VehicleStatus.AVAILABLE);
        driver.setStatus(DriverStatus.AVAILABLE);

        vehicleRepository.save(vehicle);
        driverRepository.save(driver);

        trip.setStatus(TripStatus.COMPLETED);

        tripRepository.save(trip);
    }

}