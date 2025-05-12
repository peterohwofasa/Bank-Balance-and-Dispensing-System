package com.bank.balancedispense.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global application configuration class.
 *
 * Enables and configures CORS (Cross-Origin Resource Sharing) settings
 * to allow external frontend clients (e.g., from different domains) to access API endpoints.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Configures global CORS settings for all incoming HTTP requests.
     *
     * @return a WebMvcConfigurer with custom CORS mappings
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Allow all origins, headers, and common HTTP methods for all API paths
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
