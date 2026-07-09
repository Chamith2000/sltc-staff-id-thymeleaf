package com.oexil.staffid.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "dashboard")
public class DashboardController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard() {
        return "dashboard/admin";
    }
}
