package com.bank.balancedispense.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 *
 * Sets API metadata including title, version, contact, and base server URL
 * for auto-generated Swagger UI via SpringDoc.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Defines the OpenAPI specification for the application.
     *
     * @return a configured OpenAPI bean used by Swagger UI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Balance & Dispensing System API")
                        .version("1.0.0")
                        .description("REST APIs for viewing account balances and performing ATM withdrawals")
                        .contact(new Contact()
                                .name("Bank Development Team")
                                .email("dev@bank.com"))
                )
                .servers(List.of(
                        new Server().url("/").description("Default Server URL")
                ));
    }
}
