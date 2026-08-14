package com.expensetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig {

    // Single source of truth for CORS rules, shared by Spring Security and Spring MVC
    private CorsConfiguration buildConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://pulseledger-mc.vercel.app"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // "*" covers Content-Type, Authorization (the JWT Bearer header) and
        // any other header the frontend sends
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        // Cache the preflight (OPTIONS) result for 1 hour instead of per-request
        config.setMaxAge(3600L);
        return config;
    }

    // Used directly by Spring Security's CorsFilter (see SecurityConfig.cors())
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = buildConfiguration();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    // Applied by Spring MVC to any /api/** request as well
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").combine(buildConfiguration());
            }
        };
    }
}