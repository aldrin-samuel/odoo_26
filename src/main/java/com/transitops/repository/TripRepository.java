package com.transitops.repository;

import com.transitops.entity.Trip;
import com.transitops.entity.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    // Required for Dashboard Service
    long countByStatus(TripStatus status);

    // Helper methods for getting trips by status
    List<Trip> findByStatus(TripStatus status);

    // Optional: Auto-generate unique trip number
    boolean existsByTripNumber(String tripNumber);
}