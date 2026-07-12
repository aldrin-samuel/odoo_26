package com.transitops;

import com.transitops.entity.Driver;
import com.transitops.entity.User;
import com.transitops.entity.enums.DriverStatus;
import com.transitops.repository.DriverRepository;
import com.transitops.repository.UserRepository;
import com.transitops.service.EmailService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class TransitopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransitopsApplication.class, args);
    }

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(User.builder().email("admin@transitops.com").password("admin123").name("Fleet Admin").role(User.Role.FLEET_MANAGER).active(true).build());
                userRepository.save(User.builder().email("safety@transitops.com").password("safety123").name("Jane Safety").role(User.Role.SAFETY_OFFICER).active(true).build());
                userRepository.save(User.builder().email("finance@transitops.com").password("finance123").name("Bob Finance").role(User.Role.FINANCIAL_ANALYST).active(true).build());
            }
        };
    }

    @Bean
    public CommandLineRunner checkDriverCompliance(DriverRepository driverRepository, EmailService emailService) {
        return args -> {
            LocalDate today = LocalDate.now();
            LocalDate warningThreshold = today.plusDays(30); // 30 days warning

            List<Driver> allDrivers = driverRepository.findAll();

            for (Driver d : allDrivers) {
                if (d.getLicenseExpiry() != null) {
                    // 1. Auto-suspend if expired
                    if (d.getLicenseExpiry().isBefore(today) && d.getStatus() != DriverStatus.SUSPENDED) {
                        d.setStatus(DriverStatus.SUSPENDED);
                        driverRepository.save(d);
                        emailService.sendSuspensionEmail(d.getEmail(), d.getName());
                    }
                    // 2. Send warning email if expiring within 30 days
                    else if (d.getLicenseExpiry().isBefore(warningThreshold) && d.getLicenseExpiry().isAfter(today) && d.getStatus() != DriverStatus.SUSPENDED) {
                        System.out.println("Sending 30-day expiry warning to " + d.getName());
                        emailService.sendLicenseExpiryEmail(d.getEmail(), d.getName(), d.getLicenseExpiry().toString());
                    }
                }
            }
        };
    }
}