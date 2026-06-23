package com.trainingmug.ecommerce.apigatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Slf4j
public class LoggingFilter
        implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        log.info(
                """

                ==========================================
                Incoming Request
                ==========================================
                Method       : {}
                URI          : {}
                Headers      : {}
                Host         : {}
                Query Params : {}
                Timestamp    : {}
                ==========================================

                """,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                exchange.getRequest().getHeaders(),
                exchange.getRequest()
                        .getHeaders()
                        .getHost(),
                exchange.getRequest()
                        .getQueryParams(),
                LocalDateTime.now()
        );

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
