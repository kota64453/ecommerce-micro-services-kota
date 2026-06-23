package com.ecommerce.apigatewayservice.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Auth Service is temporarily unavailable. Please try again later.",
                        "data", null
                ));
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> userFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "User Service is temporarily unavailable. Please try again later.",
                        "data", null
                ));
    }

    @GetMapping("/product")
    public ResponseEntity<Map<String, Object>> productFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Product Service is temporarily unavailable. Please try again later.",
                        "data", null
                ));
    }

    @GetMapping("/order")
    public ResponseEntity<Map<String, Object>> orderFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Order Service is temporarily unavailable. Please try again later.",
                        "data", null
                ));
    }
}
