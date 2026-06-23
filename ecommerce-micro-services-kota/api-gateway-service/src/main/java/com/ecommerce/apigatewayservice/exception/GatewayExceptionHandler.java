package com.ecommerce.apigatewayservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.apigatewayservice.dto.ErrorResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Order(-1)
@RequiredArgsConstructor
public class GatewayExceptionHandler
        implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            Throwable ex) {

        HttpStatus status =
                HttpStatus.INTERNAL_SERVER_ERROR;

        if(ex instanceof UnauthorizedException) {
            status = HttpStatus.UNAUTHORIZED;
        }
        else if(ex instanceof ForbiddenException) {
            status = HttpStatus.FORBIDDEN;
        }
        else if(ex instanceof ServiceUnavailableException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }

        ErrorResponseDto response =
                ErrorResponseDto.builder()
                        .success(false)
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(
                                exchange.getRequest()
                                        .getPath()
                                        .value()
                        )
                        .build();

        exchange.getResponse()
                .setStatusCode(status);

        exchange.getResponse()
                .getHeaders()
                .setContentType(
                        MediaType.APPLICATION_JSON
                );

        try {

            byte[] bytes =
                    objectMapper
                            .writeValueAsBytes(response);

            return exchange.getResponse()
                    .writeWith(
                            Mono.just(
                                    exchange.getResponse()
                                            .bufferFactory()
                                            .wrap(bytes)
                            )
                    );

        } catch (Exception e) {

            return Mono.error(e);
        }
    }
}