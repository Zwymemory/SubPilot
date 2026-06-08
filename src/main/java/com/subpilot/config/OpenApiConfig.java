package com.subpilot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI subPilotOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SubPilot API")
                        .description("Smart subscription and digital asset management backend")
                        .version("0.0.1")
                        .contact(new Contact().name("SubPilot"))
                        .license(new License().name("MIT")));
    }
}
