package com.totvs.wedjat.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.Map;

@SpringBootApplication
public class WedjatApplication {

    public static void main(String[] args) {
        String webDir = Path.of(System.getProperty("user.dir"), "web").toUri().toString();
        SpringApplication app = new SpringApplication(WedjatApplication.class);
        app.setDefaultProperties(Map.of(
                "server.port", "7070",
                "spring.web.resources.static-locations", webDir));
        app.run(args);
    }
}
