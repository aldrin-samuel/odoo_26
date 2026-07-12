// service/AuthService.java
package com.transitops.service;

import com.transitops.dto.LoginRequest;
import com.transitops.dto.LoginResponse;
import com.transitops.entity.User;
import com.transitops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest request) {
        // Simplified for hackathon - no password encryption
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Simple password check (for hackathon only!)
        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.getActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        String token = UUID.randomUUID().toString();

        return LoginResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    public void initDefaultUsers() {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .email("admin@transitops.com")
                    .password("admin123")  // Plain text for hackathon
                    .name("Admin User")
                    .role(User.Role.FLEET_MANAGER)
                    .active(true)
                    .build();
            userRepository.save(admin);

            User driver = User.builder()
                    .email("driver@transitops.com")
                    .password("driver123")  // Plain text for hackathon
                    .name("Driver User")
                    .role(User.Role.DRIVER)
                    .active(true)
                    .build();
            userRepository.save(driver);
        }
    }
}