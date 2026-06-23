package com.ecommerce.order.redis;

import com.ecommerce.order.dto.CartItemDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CART_PREFIX = "CART:";
    private static final long CART_TTL_HOURS = 24;

    public void addToCart(Long userId, CartItemDto item) {
        String key = CART_PREFIX + userId;
        List<CartItemDto> cartItems = getCartItems(userId);

        // Update quantity if product already in cart
        boolean found = false;
        for (CartItemDto cartItem : cartItems) {
            if (cartItem.getProductId().equals(item.getProductId())) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                found = true;
                break;
            }
        }

        if (!found) {
            cartItems.add(item);
        }

        saveCart(key, cartItems);
        log.info("Product {} added to cart for userId: {}", item.getProductId(), userId);
    }

    public void updateQuantity(Long userId, String productId, int quantity) {
        String key = CART_PREFIX + userId;
        List<CartItemDto> cartItems = getCartItems(userId);

        cartItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    if (quantity <= 0) {
                        cartItems.remove(item);
                    } else {
                        item.setQuantity(quantity);
                    }
                });

        saveCart(key, cartItems);
        log.info("Cart quantity updated for userId: {}, productId: {}", userId, productId);
    }

    public void removeFromCart(Long userId, String productId) {
        String key = CART_PREFIX + userId;
        List<CartItemDto> cartItems = getCartItems(userId);

        cartItems.removeIf(item -> item.getProductId().equals(productId));
        saveCart(key, cartItems);
        log.info("Product {} removed from cart for userId: {}", productId, userId);
    }

    public List<CartItemDto> getCart(Long userId) {
        return getCartItems(userId);
    }

    public void clearCart(Long userId) {
        String key = CART_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("Cart cleared for userId: {}", userId);
    }

    private List<CartItemDto> getCartItems(Long userId) {
        String key = CART_PREFIX + userId;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<CartItemDto>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse cart items: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveCart(String key, List<CartItemDto> items) {
        try {
            String json = objectMapper.writeValueAsString(items);
            redisTemplate.opsForValue().set(key, json, CART_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cart items: {}", e.getMessage());
        }
    }
}
