package com.trainingmug.ecommerce.apigatewayservice.config;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AuthorizationConfig {

    public Map<String, List<String>> routeRoles() {

        return Map.of(

                "GET:/api/products",
                List.of(
                        "ROLE_ADMIN",
                        "ROLE_CUSTOMER"
                ),

                "POST:/api/products",
                List.of(
                        "ROLE_ADMIN"
                ),

                "PUT:/api/products",
                List.of(
                        "ROLE_ADMIN"
                ),

                "PATCH:/api/products",
                List.of(
                        "ROLE_ADMIN"
                ),

                "DELETE:/api/products",
                List.of(
                        "ROLE_ADMIN"
                ),

                "GET:/api/users",
                List.of(
                        "ROLE_ADMIN",
                        "ROLE_CUSTOMER"
                ),

                "POST:/api/users",
                List.of(
                        "ROLE_ADMIN"
                ),

                "PUT:/api/users",
                List.of(
                        "ROLE_ADMIN"
                ),

                "DELETE:/api/users",
                List.of(
                        "ROLE_ADMIN",
                        "ROLE_CUSTOMER"
                )
        );
    }
}
