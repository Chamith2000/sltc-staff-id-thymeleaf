package com.oexil.staffid.security;

import com.oexil.staffid.exception.AccountNotApprovedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        
        logger.info("Authentication failure: Exception type = {}, Message = {}", 
                   exception.getClass().getSimpleName(), exception.getMessage());
        
        String errorMessage;
        
        if (exception instanceof AccountNotApprovedException) {
            errorMessage = exception.getMessage();
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "Invalid email or password. Please try again.";
        } else if (exception instanceof BadCredentialsException) {
            errorMessage = "Invalid email or password. Please try again.";
        } else {
            errorMessage = "Authentication failed. Please try again.";
        }
        
        // URL encode the error message
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        
        logger.info("Redirecting to login with error message: {}", errorMessage);
        logger.info("Encoded message: {}", encodedMessage);
        
        response.sendRedirect("/auth/login?error=" + encodedMessage);
    }
}