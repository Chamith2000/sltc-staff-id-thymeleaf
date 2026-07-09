package com.oexil.staffid.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Random;

@Service
public class Utils {

    public static String generateSixDigitsCode() {
        Random random = new Random();
        int randomNumber = random.nextInt(1000000);
        return String.format("%06d", randomNumber);
    }

    public static String getBaseUrl() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) return "http://localhost:8080"; // Default value if no request context
        HttpServletRequest request = attr.getRequest();

        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null) // Remove extra paths
                .build()
                .toUriString();
    }
}
