package com.ecommerce.user.controller;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.service.AuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users/auth")
@RequiredArgsConstructor
public class AuthUserController {

    private final AuthUserService authUserService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserCredentialDto>> registerUser(
            @RequestBody CreateUserCredentialRequest request) {
        log.info("{} :: Register user request for email: {}", getClass().getSimpleName(), request.getEmail());
        ApiResponse<UserCredentialDto> response = authUserService.registerUser(request);
        log.info("{} :: User registered: {}", getClass().getSimpleName(), request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserCredentialDto>> getUserByEmail(
            @PathVariable String email) {
        log.info("{} :: Get user by email request: {}", getClass().getSimpleName(), email);
        ApiResponse<UserCredentialDto> response = authUserService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserCredentialDto>> getUserById(
            @PathVariable Long id) {
        log.info("{} :: Get user by id request: {}", getClass().getSimpleName(), id);
        ApiResponse<UserCredentialDto> response = authUserService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(
            @PathVariable String email) {
        log.info("{} :: Check email exists request: {}", getClass().getSimpleName(), email);
        ApiResponse<Boolean> response = authUserService.checkEmailExists(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/email-verified")
    public ResponseEntity<ApiResponse<UserCredentialDto>> updateEmailVerified(
            @PathVariable Long id,
            @RequestBody UpdateEmailVerifiedRequest request) {
        log.info("{} :: Update email verified request for id: {}, verified: {}",
                getClass().getSimpleName(), id, request.isEmailVerified());
        ApiResponse<UserCredentialDto> response = authUserService.updateEmailVerified(id, request.isEmailVerified());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @PathVariable Long id,
            @RequestBody UpdatePasswordRequest request) {
        log.info("{} :: Update password request for id: {}", getClass().getSimpleName(), id);
        ApiResponse<Void> response = authUserService.updatePassword(id, request);
        return ResponseEntity.ok(response);
    }

    // Refresh Token endpoints

    @PostMapping("/refresh-tokens")
    public ResponseEntity<ApiResponse<RefreshTokenDto>> saveRefreshToken(
            @RequestBody SaveRefreshTokenRequest request) {
        log.info("{} :: Save refresh token for userId: {}", getClass().getSimpleName(), request.getUserId());
        ApiResponse<RefreshTokenDto> response = authUserService.saveRefreshToken(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/refresh-tokens/token/{token}")
    public ResponseEntity<ApiResponse<RefreshTokenDto>> getRefreshToken(
            @PathVariable String token) {
        log.info("{} :: Get refresh token request", getClass().getSimpleName());
        ApiResponse<RefreshTokenDto> response = authUserService.getRefreshToken(token);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/refresh-tokens/{id}")
    public ResponseEntity<ApiResponse<RefreshTokenDto>> updateRefreshToken(
            @PathVariable Long id,
            @RequestBody UpdateRefreshTokenRequest request) {
        log.info("{} :: Update refresh token id: {}, revoked: {}",
                getClass().getSimpleName(), id, request.isRevoked());
        ApiResponse<RefreshTokenDto> response = authUserService.updateRefreshToken(id, request.isRevoked());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/refresh-tokens/user/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteRefreshTokensByUserId(
            @PathVariable Long userId) {
        log.info("{} :: Delete refresh tokens for userId: {}", getClass().getSimpleName(), userId);
        ApiResponse<Void> response = authUserService.deleteRefreshTokensByUserId(userId);
        return ResponseEntity.ok(response);
    }
}
