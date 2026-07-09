package com.oexil.staffid.controller;

import com.oexil.staffid.dto.user.ForgotPassword;
import com.oexil.staffid.dto.user.ResetPassword;
import com.oexil.staffid.dto.user.UserAuth;
import com.oexil.staffid.dto.user.UserSignup;
import com.oexil.staffid.model.User;
import com.oexil.staffid.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping(value = "auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/login")
    public String login(Model model){
        model.addAttribute("userAuth", new UserAuth());
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("userSignup", new UserSignup());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String processSignup(@Valid @ModelAttribute("userSignup") UserSignup userSignup,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        try {
            User user = userService.signup(userSignup);
            redirectAttributes.addFlashAttribute("success", "Account created successfully! Your account is pending approval by the IT Admin. You will be able to login once approved.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/signup";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("forgotPassword", new ForgotPassword());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("forgotPassword") ForgotPassword forgotPassword,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            boolean sent = userService.sendPasswordResetCode(forgotPassword);
            if (sent) {
                redirectAttributes.addFlashAttribute("success", "Password reset code sent to your email. Check your inbox and enter the code below.");
                redirectAttributes.addFlashAttribute("email", forgotPassword.getEmail());
                return "redirect:/auth/reset-password";
            } else {
                model.addAttribute("error", "Failed to send reset code. Please try again.");
                return "auth/forgot-password";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPassword(Model model,
                               @RequestParam(value = "email", required = false) String email) {
        model.addAttribute("resetPassword", new ResetPassword());
        if (email != null) {
            model.addAttribute("email", email);
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("resetPassword") ResetPassword resetPassword,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        try {
            boolean reset = userService.resetPassword(resetPassword);
            if (reset) {
                redirectAttributes.addFlashAttribute("success", "Password reset successfully! You can now login with your new password.");
                return "redirect:/auth/login";
            } else {
                model.addAttribute("error", "Failed to reset password. Please try again.");
                return "auth/reset-password";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/reset-password";
        }
    }
}
