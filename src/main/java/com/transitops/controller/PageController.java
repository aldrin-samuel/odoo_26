package com.transitops.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // This handles http://localhost:8080/ and http://localhost:8080/index
    @GetMapping({"/", "/index"})
    public String showIndex() {
        return "index"; // Looks for templates/index.html
    }
}