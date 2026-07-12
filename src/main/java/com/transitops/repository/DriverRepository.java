package com.transitops.repository;

import com.transitops.entity.Driver;
import com.transitops.entity.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    boolean existsByLicenseNumber(String licenseNumber);
    long countByStatus(DriverStatus status);
    List<Driver> findByStatus(DriverStatus status);

    @Query("SELECT d FROM Driver d WHERE d.licenseExpiry < :currentDate AND d.status <> 'SUSPENDED'")
    List<Driver> findExpiredActiveDrivers(LocalDate currentDate);
}