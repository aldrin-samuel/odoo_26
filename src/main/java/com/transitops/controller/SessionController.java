package com.transitops.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/session")
@CrossOrigin(origins = "*")
public class SessionController {

    private final HttpSession session;

    public SessionController(HttpSession session) {
        this.session = session;
    }

    @GetMapping("/user")
    public Map<String, String> getCurrentUser() {
        String role = (String) session.getAttribute("role");
        String name = (String) session.getAttribute("userName");

        if (role != null) {
            return Map.of("name", name, "role", role);
        }
        return Map.of("role", "PUBLIC");
    }
}