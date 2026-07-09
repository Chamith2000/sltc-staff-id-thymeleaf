package com.oexil.staffid.controller;

import com.oexil.staffid.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public String userManagement(Model model, @RequestParam(value = "tab", defaultValue = "pending") String tab) {
        switch (tab) {
            case "approved":
                model.addAttribute("users", userService.getApprovedUsers());
                model.addAttribute("activeTab", "approved");
                break;
            case "rejected":
                model.addAttribute("users", userService.getRejectedUsers());
                model.addAttribute("activeTab", "rejected");
                break;
            default:
                model.addAttribute("users", userService.getPendingUsers());
                model.addAttribute("activeTab", "pending");
                break;
        }
        return "admin/user-management";
    }

    @PostMapping("/users/approve/{id}")
    public String approveUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (userService.approveUser(id)) {
            redirectAttributes.addFlashAttribute("success", "User approved successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to approve user");
        }
        return "redirect:/admin/users?tab=pending";
    }

    @PostMapping("/users/reject/{id}")
    public String rejectUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (userService.rejectUser(id)) {
            redirectAttributes.addFlashAttribute("success", "User rejected successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to reject user");
        }
        return "redirect:/admin/users?tab=pending";
    }
}