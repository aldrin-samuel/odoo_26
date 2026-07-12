package com.transitops.repository;

import com.transitops.entity.Driver;
import com.transitops.entity.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    boolean existsByLicenseNumber(String licenseNumber);

    // Required for Dashboard Service
    long countByStatus(DriverStatus status);

    // Required for Trip Dispatch - get only available drivers
    List<Driver> findByStatus(DriverStatus status);

    // Business Rule: Drivers with expired licenses cannot be assigned
    @Query("SELECT d FROM Driver d WHERE d.licenseExpiryDate > :currentDate AND d.status = 'AVAILABLE'")
    List<Driver> findAvailableDriversWithValidLicense(LocalDate currentDate);
}