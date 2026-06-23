package com.ecommerce.order.controller;

import com.ecommerce.order.dto.ApiResponse;
import com.ecommerce.order.dto.CartItemDto;
import com.ecommerce.order.redis.CartRedisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartRedisService cartRedisService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<List<CartItemDto>>> addToCart(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CartItemDto item) {
        cartRedisService.addToCart(userId, item);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cartRedisService.getCart(userId)));
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<ApiResponse<List<CartItemDto>>> updateQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String productId,
            @RequestParam int quantity) {
        cartRedisService.updateQuantity(userId, productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cartRedisService.getCart(userId)));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<List<CartItemDto>>> removeFromCart(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String productId) {
        cartRedisService.removeFromCart(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cartRedisService.getCart(userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDto>>> getCart(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", cartRedisService.getCart(userId)));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader("X-User-Id") Long userId) {
        cartRedisService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
