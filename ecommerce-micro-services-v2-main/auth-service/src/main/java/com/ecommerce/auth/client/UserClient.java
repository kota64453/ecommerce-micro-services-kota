package com.ecommerce.auth.client;

import com.ecommerce.auth.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", path = "/api/users")
public interface UserClient {

    // User Credential endpoints

    @PostMapping("/auth/register")
    ApiResponse<UserServiceUserDto> registerUser(@RequestBody CreateUserCredentialRequest request);

    @GetMapping("/auth/email/{email}")
    ApiResponse<UserServiceUserDto> getUserByEmail(@PathVariable String email);

    @GetMapping("/auth/{id}")
    ApiResponse<UserServiceUserDto> getUserById(@PathVariable Long id);

    @GetMapping("/auth/email/{email}/exists")
    ApiResponse<Boolean> checkEmailExists(@PathVariable String email);

    @PutMapping("/auth/{id}/email-verified")
    ApiResponse<UserServiceUserDto> updateEmailVerified(
            @PathVariable Long id,
            @RequestBody UpdateEmailVerifiedRequest request);

    @PutMapping("/auth/{id}/password")
    ApiResponse<Void> updatePassword(
            @PathVariable Long id,
            @RequestBody UpdatePasswordRequest request);

    // Refresh Token endpoints

    @PostMapping("/auth/refresh-tokens")
    ApiResponse<UserServiceRefreshTokenDto> saveRefreshToken(
            @RequestBody SaveRefreshTokenRequest request);

    @GetMapping("/auth/refresh-tokens/token/{token}")
    ApiResponse<UserServiceRefreshTokenDto> getRefreshToken(
            @PathVariable String token);

    @PutMapping("/auth/refresh-tokens/{id}")
    ApiResponse<UserServiceRefreshTokenDto> updateRefreshToken(
            @PathVariable Long id,
            @RequestBody UpdateRefreshTokenRequest request);

    @DeleteMapping("/auth/refresh-tokens/user/{userId}")
    ApiResponse<Void> deleteRefreshTokensByUserId(@PathVariable Long userId);

    // User Profile endpoint (for creating profile after registration)

    @PostMapping("/profile")
    ApiResponse<Object> createProfile(@RequestBody UserProfileDto profileData,
                                       @RequestHeader("X-User-Id") Long userId,
                                       @RequestHeader("X-User-Email") String email);
}
