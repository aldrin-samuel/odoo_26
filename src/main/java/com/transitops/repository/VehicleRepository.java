package com.transitops.repository;

import com.transitops.entity.Vehicle;
import com.transitops.entity.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByRegistrationNumber(String registrationNumber);

    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    // Required for Dashboard Service
    long countByStatus(VehicleStatus status);

    // Required for Trip Dispatch - get only available vehicles
    List<Vehicle> findByStatus(VehicleStatus status);
}