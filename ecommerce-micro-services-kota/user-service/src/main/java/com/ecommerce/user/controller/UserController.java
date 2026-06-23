package com.ecommerce.user.controller;

import com.ecommerce.user.dto.AddressDto;
import com.ecommerce.user.dto.ApiResponse;
import com.ecommerce.user.dto.UserProfileDto;
import com.ecommerce.user.dto.WishlistItemDto;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> createProfile(@RequestBody UserProfileDto profileDto,
                                                                      @RequestHeader("X-User-Id") Long userId,
                                                                      @RequestHeader("X-User-Email") String email) {
        log.info("{} :: Create profile request for userId: {}, email: {}", getClass().getSimpleName(), userId, email);
        ApiResponse<UserProfileDto> response = userService.createProfile(profileDto);
        log.info("{} :: Profile created for userId: {}", getClass().getSimpleName(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(@RequestHeader("X-User-Id") Long userId) {
        log.info("{} :: Get profile request for userId: {}", getClass().getSimpleName(), userId);
        ApiResponse<UserProfileDto> response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UserProfileDto profileDto) {
        log.info("{} :: Update profile request for userId: {}", getClass().getSimpleName(), userId);
        ApiResponse<UserProfileDto> response = userService.updateProfile(userId, profileDto);
        log.info("{} :: Profile updated for userId: {}", getClass().getSimpleName(), userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/email/{email}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfileByEmail(@PathVariable String email) {
        log.info("{} :: Get profile by email request: {}", getClass().getSimpleName(), email);
        ApiResponse<UserProfileDto> response = userService.getProfileByEmail(email);
        return ResponseEntity.ok(response);
    }

    // Address endpoints

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<AddressDto>> addAddress(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddressDto addressDto) {
        log.info("{} :: Add address request for userId: {}", getClass().getSimpleName(), userId);
        ApiResponse<AddressDto> response = userService.addAddress(userId, addressDto);
        log.info("{} :: Address added for userId: {}", getClass().getSimpleName(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<AddressDto>>> getAddresses(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("{} :: Get addresses request for userId: {}", getClass().getSimpleName(), userId);
        ApiResponse<List<AddressDto>> response = userService.getAddresses(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDto addressDto) {
        log.info("{} :: Update address request for userId: {}, addressId: {}", getClass().getSimpleName(), userId, addressId);
        ApiResponse<AddressDto> response = userService.updateAddress(userId, addressId, addressDto);
        log.info("{} :: Address updated for userId: {}", getClass().getSimpleName(), userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId) {
        log.info("{} :: Delete address request for userId: {}, addressId: {}", getClass().getSimpleName(), userId, addressId);
        ApiResponse<Void> response = userService.deleteAddress(userId, addressId);
        log.info("{} :: Address deleted for userId: {}", getClass().getSimpleName(), userId);
        return ResponseEntity.ok(response);
    }

    // Wishlist endpoints

    @GetMapping("/wishlist")
    public ResponseEntity<ApiResponse<List<WishlistItemDto>>> getWishlist(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("{} :: Get wishlist request for userId: {}", getClass().getSimpleName(), userId);
        ApiResponse<List<WishlistItemDto>> response = userService.getWishlist(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<ApiResponse<WishlistItemDto>> addToWishlist(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String productId) {
        log.info("{} :: Add to wishlist request for userId: {}, productId: {}", getClass().getSimpleName(), userId, productId);
        ApiResponse<WishlistItemDto> response = userService.addToWishlist(userId, productId);
        log.info("{} :: Product added to wishlist for userId: {}", getClass().getSimpleName(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String productId) {
        log.info("{} :: Remove from wishlist request for userId: {}, productId: {}", getClass().getSimpleName(), userId, productId);
        ApiResponse<Void> response = userService.removeFromWishlist(userId, productId);
        log.info("{} :: Product removed from wishlist for userId: {}", getClass().getSimpleName(), userId);
        return ResponseEntity.ok(response);
    }
}
