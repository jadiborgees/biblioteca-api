package com.biblioteca.config;

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
                registry.addMapping("/**")
                        // Usar allowedOriginPatterns em vez de allowedOrigins para aceitar subdomínios e IPs de redes locais (React Native/Expo)
                        .allowedOriginPatterns(
                                "http://localhost:[*]",           // Cobre 3000, 4200, 8081, etc.
                                "http://127.0.0.1:[*]",
                                "http://10.0.2.2:[*]",           // Emulador Android Studio
                                "http://192.168.*:[*]",          // Dispositivo físico na rede Wi-Fi local
                                "https://*.vercel.app"            // Ambientes de preview e prod na Vercel
                        )
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders(
                                "Authorization",
                                "Content-Type",
                                "X-API-Key",
                                "X-Idempotency-Key",
                                "X-API-Version"
                        )
                        .exposedHeaders(
                                "Location",                       // Importante para retornos de POST (201 Created)
                                "Retry-After",
                                "X-API-Version",
                                "X-Idempotency-Key"
                        )
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}