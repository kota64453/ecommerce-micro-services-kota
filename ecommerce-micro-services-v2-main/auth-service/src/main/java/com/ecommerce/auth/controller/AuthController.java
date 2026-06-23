package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("{} :: Received signup request for email: {}", getClass().getSimpleName(), request.getEmail());
        ApiResponse<Void> response = authService.signup(request);
        log.info("{} :: Signup response: success={}, message={}", getClass().getSimpleName(), response.isSuccess(), response.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        log.info("{} :: Received OTP verification request for email: {}", getClass().getSimpleName(), request.getEmail());
        ApiResponse<AuthResponse> response = authService.verifyOtp(request);
        log.info("{} :: OTP verification response: success={}", getClass().getSimpleName(), response.isSuccess());
        return ResponseEntity.ok(response);
    }

   // @PostMapping("/complete-signup")
  //  public ResponseEntity<ApiResponse<AuthResponse>> completeSignup(
      //      @Valid @RequestBody SignupRequest request) {
        //log.info("{} :: Received complete signup request for email: {}", getClass().getSimpleName(), request.getEmail());
        //ApiResponse<AuthResponse> response = authService.completeSignup(request);
        //log.info("{} :: Complete signup successful for email: {}", getClass().getSimpleName(), request.getEmail());
        //return ResponseEntity.status(HttpStatus.CREATED)
          //      .body(response);
   // }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("{} :: Received login request for email: {}", getClass().getSimpleName(), request.getEmail());
        ApiResponse<AuthResponse> response = authService.login(request);
        log.info("{} :: Login successful: {}", getClass().getSimpleName(), request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody(required = false) RefreshTokenRequest request) {
        String refreshToken = request != null ? request.getRefreshToken() : "";
        log.info("{} :: Received logout request - userId: {}, refreshToken present: {}",
                getClass().getSimpleName(), userId, request != null);
        ApiResponse<Void> response = authService.logout(userId, refreshToken);
        log.info("{} :: Logout successful for userId: {}", getClass().getSimpleName(), userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("{} :: Received forgot password request for email: {}", getClass().getSimpleName(), request.getEmail());
        ApiResponse<Void> response = authService.forgotPassword(request);
        log.info("{} :: Forgot password OTP sent for email: {}", getClass().getSimpleName(), request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("{} :: Received reset password request for email: {}", getClass().getSimpleName(), request.getEmail());
        ApiResponse<Void> response = authService.resetPassword(request);
        log.info("{} :: Password reset successful for email: {}", getClass().getSimpleName(), request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("{} :: Received refresh token request", getClass().getSimpleName());
        ApiResponse<AuthResponse> response = authService.refreshToken(request);
        log.info("{} :: Token refreshed successfully", getClass().getSimpleName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("{} :: Received resend OTP request for email: {}", getClass().getSimpleName(), request.getEmail());
        ApiResponse<Void> response = authService.resendOtp(request);
        log.info("{} :: OTP resent to email: {}", getClass().getSimpleName(), request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<UserDto>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        log.info("{} :: Received validate token request", getClass().getSimpleName());
        ApiResponse<UserDto> response = authService.validateToken(token);
        log.info("{} :: Token validation response: success={}", getClass().getSimpleName(), response.isSuccess());
        return ResponseEntity.ok(response);
    }
}
