package com.ecommerce.notification.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Notification Service API",
                description = "Notification service with RabbitMQ consumer and email sending",
                version = "1.0.0",
                contact = @Contact(name = "E-Commerce Team", email = "support@ecommerce.com")
        ),
        servers = @Server(url = "http://localhost:8088", description = "Local")
)
public class OpenApiConfig {
}
