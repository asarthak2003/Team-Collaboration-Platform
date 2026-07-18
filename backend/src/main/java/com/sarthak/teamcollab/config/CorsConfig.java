package com.sarthak.teamcollab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // allow frontend app to connect to the backend
                registry.addMapping("/**").allowedOriginPatterns("*").allowedMethods("GET", "POST", "PUT",
                        "DELETE", "OPTIONS")
                        // In production restrict this to your frontend URL
                        .allowedHeaders("*").allowCredentials(true);
            }
        };
    }
}
