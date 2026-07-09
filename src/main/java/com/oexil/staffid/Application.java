package com.oexil.staffid;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application implements WebMvcConfigurer {

    @Value("${archive.path}")
    private String archivePath;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    private void createArchiveIfNotExists() {
        File directory = new File(archivePath);
        if (! directory.exists()){
            directory.mkdir();
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Register resource handler for images
        registry.addResourceHandler("/files/**").addResourceLocations("file:///"+archivePath+"/")
                .setCacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic());
    }
}
