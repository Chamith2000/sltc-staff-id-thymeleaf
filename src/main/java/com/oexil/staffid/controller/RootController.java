package com.oexil.staffid.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String redirectToDashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login"; // Redirect to login if not authenticated
        }

        // Check user roles
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MANAGER"));
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));

        // Redirect based on role
        if (isAdmin) {
            return "redirect:/staff/employee-list";
        }

        // Default fallback for unknown roles
        return "redirect:/auth/login";
    }
}
