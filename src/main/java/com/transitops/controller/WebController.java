package com.transitops.controller;

import com.transitops.entity.User;
import com.transitops.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebController {

    private final UserRepository userRepository;

    public WebController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Handles http://localhost:8080/
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    // Handles http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {
        return "login"; // Looks for templates/login.html
    }

    // Handles the login form submission
    @PostMapping("/login")
    public String loginSubmit(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null && user.getPassword().equals(password) && user.getActive()) {
                session.setAttribute("userId", user.getId());
                session.setAttribute("userName", user.getName());
                session.setAttribute("role", user.getRole().name());
                return "redirect:/index";
            }

            model.addAttribute("error", "Invalid email or password");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "login";
        }
    }

    // Handles http://localhost:8080/index
    @GetMapping("/index")
    public String showIndex(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");

        // If they aren't logged in, kick them back to login
        if (role == null) {
            return "redirect:/login";
        }

        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("role", role);

        return "index"; // Looks for templates/index.html
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}