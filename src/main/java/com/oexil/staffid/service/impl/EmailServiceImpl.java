package com.oexil.staffid.service.impl;

import com.oexil.staffid.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    
    private static final String EMAIL_SIGNATURE = 
        "Best regards,\n" +
        "Manoj Hasaranga\n" +
        "IT Department\n" +
        "0707432736\n" +
        "chathura.m@sltc.ac.lk\n" +
        "Sri Lanka Technological Campus";

    @Override
    public boolean sendPasswordResetEmail(String toEmail, String userName, String resetCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("staffid@sltc.ac.lk");
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - SLTC Staff ID System");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "We received a request to reset your password for the SLTC Staff ID System.\n\n" +
                "Your password reset code is: %s\n\n" +
                "Please use this code to reset your password. This code will expire in 30 minutes for security reasons.\n\n" +
                "If you did not request this password reset, please ignore this email or contact the system administrator.\n\n" +
                "%s",
                userName, resetCode, EMAIL_SIGNATURE
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Password reset email sent successfully to: {}", toEmail);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}. Error: {}", toEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendWelcomeEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("staffid@sltc.ac.lk");
            message.setTo(toEmail);
            message.setSubject("Welcome to SLTC Staff ID System");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Welcome to the SLTC Staff ID System!\n\n" +
                "Your account has been successfully created and is currently pending approval by the System Administrator.\n\n" +
                "You will receive another email once your account is approved and you can start using the system.\n\n" +
                "If you have any questions, please contact the IT Department.\n\n" +
                "%s",
                userName, EMAIL_SIGNATURE
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Welcome email sent successfully to: {}", toEmail);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}. Error: {}", toEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendAccountApprovalEmail(String toEmail, String userName, boolean approved) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("staffid@sltc.ac.lk");
            message.setTo(toEmail);
            
            if (approved) {
                message.setSubject("Account Approved - SLTC Staff ID System");
                String emailBody = String.format(
                    "Dear %s,\n\n" +
                    "Great news! Your account for the SLTC Staff ID System has been approved.\n\n" +
                    "You can now log in to the system using your email address and password.\n\n" +
                    "Login URL: https://studentid:8081/auth/login\n\n" +
                    "If you have any questions or need assistance, please contact the IT Department.\n\n" +
                    "%s",
                    userName, EMAIL_SIGNATURE
                );
                message.setText(emailBody);
            } else {
                message.setSubject("Account Status - SLTC Staff ID System");
                String emailBody = String.format(
                    "Dear %s,\n\n" +
                    "We regret to inform you that your account application for the SLTC Staff ID System has been declined.\n\n" +
                    "If you believe this is an error or need further clarification, please contact the IT Department.\n\n" +
                    "%s",
                    userName, EMAIL_SIGNATURE
                );
                message.setText(emailBody);
            }
            
            mailSender.send(message);
            
            log.info("Account {} email sent successfully to: {}", approved ? "approval" : "rejection", toEmail);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send account {} email to: {}. Error: {}", approved ? "approval" : "rejection", toEmail, e.getMessage());
            return false;
        }
    }
}