package com.trainingmug.ecommerce.apigatewayservice.filter;

import com.trainingmug.ecommerce.apigatewayservice.config.AuthorizationConfig;
import com.trainingmug.ecommerce.apigatewayservice.exception.ForbiddenException;
import com.trainingmug.ecommerce.apigatewayservice.exception.UnauthorizedException;
import com.trainingmug.ecommerce.apigatewayservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter
        implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private final AuthorizationConfig authorizationConfig;

    private static final List<String> PUBLIC_APIS =
            List.of(
                    "/api/auth/login",
                    "/api/auth/signup",
                    "/swagger-ui",
                    "/v3/api-docs"
            );

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String path =
                exchange.getRequest()
                        .getURI()
                        .getPath();

        boolean isPublicApi =
                PUBLIC_APIS.stream()
                        .anyMatch(path::startsWith);

        if (isPublicApi) {
            log.info("public api : {} ", path);
            return chain.filter(exchange);
        }

        String authorizationHeader =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(
                                HttpHeaders.AUTHORIZATION
                        );

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            throw new UnauthorizedException(
                    "Authorization Header Missing"
            );
        }

        String token =
                authorizationHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {

            throw new UnauthorizedException(
                    "Invalid JWT Token"
            );
        }

        String email =
                jwtUtil.retrieveEmailFromToken(token);

        List<String> roles =
                jwtUtil.retrieveRolesFromToken(token);

        log.info(
                "Authenticated User : {}",
                email
        );

        log.info(
                "Roles : {}",
                roles
        );

        String method =
                exchange.getRequest()
                        .getMethod()
                        .name();

        String route =
                determineRoute(path);

        String routeKey =
                method + ":" + route;

        List<String> allowedRoles =
                authorizationConfig
                        .routeRoles()
                        .get(routeKey);

        if (allowedRoles != null) {

            boolean authorized =
                    roles.stream()
                            .anyMatch(
                                    allowedRoles::contains
                            );

            if (!authorized) {

                throw new ForbiddenException(
                        "Access Denied"
                );
            }
        }

        return chain.filter(exchange);
    }

    private String determineRoute(
            String path) {

        if (path.startsWith("/api/products")) {

            return "/api/products";
        }

        if (path.startsWith("/api/users")) {

            return "/api/users";
        }

        return "";
    }

    @Override
    public int getOrder() {
        return 2;
    }
}

