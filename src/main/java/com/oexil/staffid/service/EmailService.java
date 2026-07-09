package com.oexil.staffid.service;

public interface EmailService {
    boolean sendPasswordResetEmail(String toEmail, String userName, String resetCode);
    boolean sendWelcomeEmail(String toEmail, String userName);
    boolean sendAccountApprovalEmail(String toEmail, String userName, boolean approved);
}