package com.transitops;

import com.transitops.entity.User;
import com.transitops.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TransitopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransitopsApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(User.builder().email("admin@transitops.com").password("admin123").name("Fleet Admin").role(User.Role.FLEET_MANAGER).active(true).build());
                userRepository.save(User.builder().email("driver@transitops.com").password("driver123").name("John Driver").role(User.Role.DRIVER).active(true).build());
                userRepository.save(User.builder().email("safety@transitops.com").password("safety123").name("Jane Safety").role(User.Role.SAFETY_OFFICER).active(true).build());
                userRepository.save(User.builder().email("finance@transitops.com").password("finance123").name("Bob Finance").role(User.Role.FINANCIAL_ANALYST).active(true).build());
            }
        };
    }
}