package com.oexil.staffid.constants;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class Constants {
    public static String IMAGE_URL;

    public Constants(Environment env) {
        String port = env.getProperty("server.port", "8080");
        IMAGE_URL = "http://localhost:" + port + "/";
    }
}
