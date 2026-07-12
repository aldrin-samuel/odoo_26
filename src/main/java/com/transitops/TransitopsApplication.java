package com.transitops;

import com.transitops.entity.Driver;
import com.transitops.entity.User;
import com.transitops.entity.enums.DriverStatus;
import com.transitops.repository.DriverRepository;
import com.transitops.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    // FEATURE 4: Auto-suspend drivers with expired licenses
    @Bean
    public CommandLineRunner suspendExpiredDrivers(DriverRepository driverRepository) {
        return args -> {
            List<Driver> expiredDrivers = driverRepository.findExpiredActiveDrivers(LocalDate.now());
            if (!expiredDrivers.isEmpty()) {
                expiredDrivers.forEach(d -> d.setStatus(DriverStatus.SUSPENDED));
                driverRepository.saveAll(expiredDrivers);
                System.out.println("Auto-suspended " + expiredDrivers.size() + " drivers with expired licenses.");
            }
        };
    }
}