package com.transitops.service;

import com.transitops.entity.Driver;
import com.transitops.entity.User;
import com.transitops.entity.enums.DriverStatus;
import com.transitops.repository.DriverRepository;
import com.transitops.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public DriverService(DriverRepository driverRepository, UserRepository userRepository, EmailService emailService) {
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Map<String, Object> addDriver(Driver driver) {
        if (driverRepository.existsByLicenseNumber(driver.getLicenseNumber())) {
            throw new RuntimeException("License number already exists");
        }

        driver.setStatus(DriverStatus.AVAILABLE);
        driver = driverRepository.save(driver);

        // Generate Login Credentials
        String baseName = driver.getName().toLowerCase().replaceAll("\\s+", ".");
        String username = baseName + "." + driver.getId() + "@transitops.com";
        String password = UUID.randomUUID().toString().substring(0, 8);

        User user = User.builder()
                .email(username)
                .password(password)
                .name(driver.getName())
                .role(User.Role.DRIVER)
                .active(true)
                .build();
        userRepository.save(user);

        // Return driver data AND credentials to show on frontend
        Map<String, Object> result = new HashMap<>();
        result.put("id", driver.getId());
        result.put("name", driver.getName());
        result.put("email", driver.getEmail());
        result.put("licenseNumber", driver.getLicenseNumber());
        result.put("licenseExpiry", driver.getLicenseExpiry());
        result.put("contactNumber", driver.getContactNumber());
        result.put("status", driver.getStatus().name());
        result.put("generatedEmail", username);
        result.put("generatedPassword", password);

        return result;
    }

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @Transactional
    public void suspendDriver(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getStatus() == DriverStatus.ON_TRIP) {
            throw new RuntimeException("Cannot suspend a driver who is currently on a trip!");
        }

        driver.setStatus(DriverStatus.SUSPENDED);
        driverRepository.save(driver);

        // Trigger Email Notification
        emailService.sendSuspensionEmail(driver.getEmail(), driver.getName());
    }

    @Transactional
    public void reactivateDriver(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getStatus() != DriverStatus.SUSPENDED) {
            throw new RuntimeException("Only suspended drivers can be reactivated");
        }

        // Safety check: Cannot reactivate if license is STILL expired
        if (driver.getLicenseExpiry().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot reactivate: License is expired. Update license expiry date first.");
        }

        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);
    }

    @Transactional
    public void updateLicenseExpiry(Long id, String newExpiryDate) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        driver.setLicenseExpiry(LocalDate.parse(newExpiryDate));

        // Auto-Unsuspend: If license is now valid and they are suspended, make them available
        if (driver.getLicenseExpiry().isAfter(LocalDate.now()) && driver.getStatus() == DriverStatus.SUSPENDED) {
            driver.setStatus(DriverStatus.AVAILABLE);
        }

        driverRepository.save(driver);
    }

    @Transactional
    public void setOffDuty(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getStatus() == DriverStatus.ON_TRIP) {
            throw new RuntimeException("Cannot set a driver to Off Duty while they are on a trip!");
        }

        driver.setStatus(DriverStatus.OFF_DUTY);
        driverRepository.save(driver);
    }

    @Transactional
    public void setAvailable(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getStatus() == DriverStatus.ON_TRIP) {
            throw new RuntimeException("Cannot set a driver to Available while they are on a trip!");
        }

        // Safety check: Cannot make them available if license is expired
        if (driver.getLicenseExpiry().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot set to Available: License is expired. Update license expiry date first.");
        }

        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);
    }

    public void deleteDriver(Long id) {
        driverRepository.deleteById(id);
    }
}