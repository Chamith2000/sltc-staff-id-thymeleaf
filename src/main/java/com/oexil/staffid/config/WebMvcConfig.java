package com.oexil.staffid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = "uploads/";
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        String uploadLocation = uploadPath.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }
}
