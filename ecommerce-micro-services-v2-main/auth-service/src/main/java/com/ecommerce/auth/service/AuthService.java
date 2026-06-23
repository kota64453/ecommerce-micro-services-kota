package com.ecommerce.auth.service;

import com.ecommerce.auth.client.*;
import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.event.ForgotPasswordEvent;
import com.ecommerce.auth.event.OtpEvent;
import com.ecommerce.auth.event.WelcomeEvent;
import com.ecommerce.auth.exception.BusinessException;
import com.ecommerce.auth.exception.JwtException;
import com.ecommerce.auth.exception.ResourceNotFoundException;
import com.ecommerce.auth.producer.EventProducer;
import com.ecommerce.auth.redis.SignupRedisService;
import com.ecommerce.auth.util.JwtUtil;
import com.ecommerce.auth.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final SignupRedisService signupRedisService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpUtil otpUtil;
    private final EventProducer eventProducer;
    private final StringRedisTemplate redisTemplate;
    private static final String SIGNUP_PREFIX = "SIGNUP:";
    private static final long SIGNUP_TTL_HOURS = 1;

    public ApiResponse<Void> signup(SignupRequest request) {
        log.info("{} :: Signup request for email: {}", getClass().getSimpleName(), request.getEmail());

        // Check if email already exists in user-service
        try {
            ApiResponse<Boolean> emailCheck = userClient.checkEmailExists(request.getEmail());
            if (emailCheck.getData() != null && emailCheck.getData()) {
                log.warn("{} :: Email already registered: {}", getClass().getSimpleName(), request.getEmail());
                throw new BusinessException("Email already registered");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("{} :: Error checking email existence: {}", getClass().getSimpleName(), e.getMessage());
            throw new BusinessException("User service unavailable. Please try again later.");
        }

        String otp = otpUtil.generateOtp();
        signupRedisService.storeOtp(request.getEmail(), otp);

        OtpEvent otpEvent = OtpEvent.builder()
                .email(request.getEmail())
                .otp(otp)
                .name(request.getName())
                .eventType("OTP_EVENT")
                .build();
        eventProducer.publishOtpEvent(otpEvent);

        log.info("{} :: OTP sent to email: {}", getClass().getSimpleName(), request.getEmail());
        return ApiResponse.success("OTP sent to your email. Please verify to complete registration.");
    }


    public ApiResponse<AuthResponse> verifyOtp(VerifyOtpRequest request) {

        log.info("{} :: OTP verification for email: {}",
                getClass().getSimpleName(),
                request.getEmail());

        boolean isValid = signupRedisService.validateOtp(
                request.getEmail(),
                request.getOtp());

        if (!isValid) {
            log.warn("{} :: Invalid or expired OTP for email: {}",
                    getClass().getSimpleName(),
                    request.getEmail());

            throw new BusinessException("Invalid or expired OTP");
        }

        // Fetch signup request from Redis
        SignupRequest signupRequest = signupRedisService.getSignupRequest(request.getEmail());


        // Create user in user-service
        UserServiceUserDto userDto;
        try {

            CreateUserCredentialRequest createRequest =
                    CreateUserCredentialRequest.builder()
                            .email(signupRequest.getEmail())
                            .password(passwordEncoder.encode(
                                    signupRequest.getPassword()))
                            .name(signupRequest.getName())
                            .phone(signupRequest.getPhone())
                            .role("ROLE_USER")
                            .build();

            ApiResponse<UserServiceUserDto> response =
                    userClient.registerUser(createRequest);

            userDto = response.getData();

            log.info("{} :: User created in user-service with id: {}",
                    getClass().getSimpleName(),
                    userDto.getId());

        } catch (Exception e) {

            log.error("{} :: Error registering user in user-service: {}",
                    getClass().getSimpleName(),
                    e.getMessage());

            throw new BusinessException(
                    "Error creating user. Please try again.");
        }

        // Mark email as verified
        try {

            userClient.updateEmailVerified(
                    userDto.getId(),
                    UpdateEmailVerifiedRequest.builder()
                            .emailVerified(true)
                            .build());

            log.info("{} :: Email verified for userId: {}",
                    getClass().getSimpleName(),
                    userDto.getId());

        } catch (Exception e) {

            log.error("{} :: Error updating email verified status: {}",
                    getClass().getSimpleName(),
                    e.getMessage());
        }

        // Create profile
        try {

            UserProfileDto profileDto =
                    UserProfileDto.builder()
                            .id(userDto.getId())
                            .email(userDto.getEmail())
                            .name(userDto.getName())
                            .phone(userDto.getPhone())
                            .build();

            userClient.createProfile(
                    profileDto,
                    userDto.getId(),
                    userDto.getEmail());

            log.info("{} :: User profile created for userId: {}",
                    getClass().getSimpleName(),
                    userDto.getId());

        } catch (Exception e) {

            log.warn("{} :: Could not create user profile: {}",
                    getClass().getSimpleName(),
                    e.getMessage());
        }

        // Generate JWT tokens
        String accessToken =
                jwtUtil.generateAccessToken(userDto);

        String refreshToken =
                jwtUtil.generateRefreshToken(userDto);

        // Save refresh token
        saveRefreshToken(refreshToken, userDto);

        // Publish welcome event
        WelcomeEvent welcomeEvent =
                WelcomeEvent.builder()
                        .email(userDto.getEmail())
                        .name(userDto.getName())
                        .phone(userDto.getPhone())
                        .eventType("WELCOME_EVENT")
                        .build();

        eventProducer.publishWelcomeEvent(welcomeEvent);

        // Cleanup Redis
        try {
            signupRedisService.deleteSignupData(request.getEmail());
            signupRedisService.deleteOtp(request.getEmail());
        } catch (Exception e) {
            log.warn("{} :: Error cleaning Redis data: {}",
                    getClass().getSimpleName(),
                    e.getMessage());
        }

        AuthResponse authResponse =
                buildAuthResponse(
                        accessToken,
                        refreshToken,
                        userDto);

        log.info("{} :: User registered successfully: {}",
                getClass().getSimpleName(),
                userDto.getEmail());

        return ApiResponse.success(
                "Registration successful",
                authResponse);
    }


    public ApiResponse<AuthResponse> login(LoginRequest request) {
        log.info("{} :: Login request for email: {}", getClass().getSimpleName(), request.getEmail());

        UserServiceUserDto userDto;
        try {
            ApiResponse<UserServiceUserDto> response = userClient.getUserByEmail(request.getEmail());
            userDto = response.getData();
            log.info("{} :: User found: {}", getClass().getSimpleName(), request.getEmail());
        } catch (Exception e) {
            log.warn("{} :: User not found or service error: {}", getClass().getSimpleName(), request.getEmail());
            throw new BusinessException("Invalid email or password");
        }

        // Verify password using the encoded password from user-service
        if (!passwordEncoder.matches(request.getPassword(), userDto.getPassword())) {
            log.warn("{} :: Invalid password for email: {}", getClass().getSimpleName(), request.getEmail());
            throw new BusinessException("Invalid email or password");
        }

        if (!userDto.isEnabled()) {
            log.warn("{} :: Account is disabled for email: {}", getClass().getSimpleName(), request.getEmail());
            throw new BusinessException("Account is disabled. Please contact support.");
        }

        String accessToken = jwtUtil.generateAccessToken(userDto);
        String refreshToken = jwtUtil.generateRefreshToken(userDto);

        saveRefreshToken(refreshToken, userDto);

        AuthResponse response = buildAuthResponse(accessToken, refreshToken, userDto);
        log.info("{} :: User logged in successfully: {}", getClass().getSimpleName(), userDto.getEmail());

        return ApiResponse.success("Login successful", response);
    }

    public ApiResponse<Void> logout(Long userId, String refreshToken) {
        log.info("{} :: Logout request for userId: {}", getClass().getSimpleName(), userId);

        try {
            userClient.deleteRefreshTokensByUserId(userId);
            log.info("{} :: Refresh tokens deleted for userId: {}", getClass().getSimpleName(), userId);
        } catch (Exception e) {
            log.error("{} :: Error deleting refresh tokens: {}", getClass().getSimpleName(), e.getMessage());
            throw new BusinessException("Error during logout");
        }

        return ApiResponse.success("Logged out successfully");
    }

    public ApiResponse<Void> forgotPassword(ForgotPasswordRequest request) {
        log.info("{} :: Forgot password request for email: {}", getClass().getSimpleName(), request.getEmail());

        UserServiceUserDto userDto;
        try {
            ApiResponse<UserServiceUserDto> response = userClient.getUserByEmail(request.getEmail());
            userDto = response.getData();
        } catch (Exception e) {
            log.warn("{} :: User not found for forgot password: {}", getClass().getSimpleName(), request.getEmail());
            throw new ResourceNotFoundException("User", "email", request.getEmail());
        }

        String otp = otpUtil.generateOtp();
        signupRedisService.storeOtp(request.getEmail(), otp);

        ForgotPasswordEvent event = ForgotPasswordEvent.builder()
                .email(request.getEmail())
                .otp(otp)
                .name(userDto.getName())
                .eventType("FORGOT_PASSWORD_EVENT")
                .build();
        eventProducer.publishForgotPasswordEvent(event);

        log.info("{} :: Password reset OTP sent to email: {}", getClass().getSimpleName(), request.getEmail());
        return ApiResponse.success("Password reset OTP sent to your email");
    }

    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {
        log.info("{} :: Reset password for email: {}", getClass().getSimpleName(), request.getEmail());

        boolean isValid = signupRedisService.validateOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            log.warn("{} :: Invalid or expired OTP for email: {}", getClass().getSimpleName(), request.getEmail());
            throw new BusinessException("Invalid or expired OTP");
        }

        UserServiceUserDto userDto;
        try {
            ApiResponse<UserServiceUserDto> response = userClient.getUserByEmail(request.getEmail());
            userDto = response.getData();
        } catch (Exception e) {
            log.warn("{} :: User not found for reset password: {}", getClass().getSimpleName(), request.getEmail());
            throw new ResourceNotFoundException("User", "email", request.getEmail());
        }

        // Update password in user-service
        try {
            UpdatePasswordRequest updateRequest = UpdatePasswordRequest.builder()
                    .newPassword(passwordEncoder.encode(request.getNewPassword()))
                    .build();
            userClient.updatePassword(userDto.getId(), updateRequest);
            log.info("{} :: Password reset successfully for email: {}", getClass().getSimpleName(), request.getEmail());
        } catch (Exception e) {
            log.error("{} :: Error updating password in user-service: {}", getClass().getSimpleName(), e.getMessage());
            throw new BusinessException("Error resetting password. Please try again.");
        }

        return ApiResponse.success("Password reset successfully");
    }

    public ApiResponse<AuthResponse> refreshToken(RefreshTokenRequest request) {
        log.info("{} :: Refresh token request", getClass().getSimpleName());

        UserServiceRefreshTokenDto storedToken;
        try {
            ApiResponse<UserServiceRefreshTokenDto> response = userClient.getRefreshToken(request.getRefreshToken());
            storedToken = response.getData();
            log.info("{} :: Refresh token found", getClass().getSimpleName());
        } catch (Exception e) {
            log.warn("{} :: Invalid refresh token: {}", getClass().getSimpleName(), e.getMessage());
            throw new BusinessException("Invalid refresh token");
        }

        if (storedToken.isRevoked()) {
            log.warn("{} :: Refresh token has been revoked", getClass().getSimpleName());
            throw new BusinessException("Refresh token has been revoked");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("{} :: Refresh token has expired", getClass().getSimpleName());
            throw new BusinessException("Refresh token has expired");
        }

        UserServiceUserDto userDto;
        try {
            ApiResponse<UserServiceUserDto> response = userClient.getUserById(storedToken.getUserId());
            userDto = response.getData();
        } catch (Exception e) {
            log.error("{} :: User not found: {}", getClass().getSimpleName(), e.getMessage());
            throw new ResourceNotFoundException("User", "id", storedToken.getUserId());
        }

        // Revoke old token in user-service
        try {
            UpdateRefreshTokenRequest updateRequest = UpdateRefreshTokenRequest.builder()
                    .revoked(true)
                    .build();
            userClient.updateRefreshToken(storedToken.getId(), updateRequest);
            log.info("{} :: Old refresh token revoked for userId: {}", getClass().getSimpleName(), userDto.getId());
        } catch (Exception e) {
            log.error("{} :: Error revoking old refresh token: {}", getClass().getSimpleName(), e.getMessage());
        }

        // Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(userDto);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDto);
        saveRefreshToken(newRefreshToken, userDto);

        AuthResponse response = buildAuthResponse(newAccessToken, newRefreshToken, userDto);
        log.info("{} :: Token refreshed successfully for user: {}", getClass().getSimpleName(), userDto.getEmail());

        return ApiResponse.success("Token refreshed successfully", response);
    }

    public ApiResponse<Void> resendOtp(ForgotPasswordRequest request) {
        log.info("{} :: Resend OTP for email: {}", getClass().getSimpleName(), request.getEmail());

        String name = "User";
        try {
            ApiResponse<UserServiceUserDto> response = userClient.getUserByEmail(request.getEmail());
            if (response.getData() != null) {
                name = response.getData().getName();
            }
        } catch (Exception e) {
            log.warn("{} :: User not found for resend OTP, using default name", getClass().getSimpleName());
        }

        String otp = otpUtil.generateOtp();
        signupRedisService.storeOtp(request.getEmail(), otp);

        OtpEvent otpEvent = OtpEvent.builder()
                .email(request.getEmail())
                .otp(otp)
                .name(name)
                .eventType("OTP_EVENT")
                .build();
        eventProducer.publishOtpEvent(otpEvent);

        log.info("{} :: OTP resent to email: {}", getClass().getSimpleName(), request.getEmail());
        return ApiResponse.success("OTP resent to your email");
    }

    public ApiResponse<UserDto> validateToken(String token) {
        log.info("{} :: Validating token", getClass().getSimpleName());

        boolean isValid = jwtUtil.validateToken(token);
        if (!isValid) {
            log.warn("{} :: Token validation failed", getClass().getSimpleName());
            throw new JwtException("Invalid or expired token");
        }

        String userId = jwtUtil.getUserIdFromToken(token);
        log.info("{} :: Token valid for userId: {}", getClass().getSimpleName(), userId);

        UserServiceUserDto userDto;
        try {
            ApiResponse<UserServiceUserDto> response = userClient.getUserById(Long.parseLong(userId));
            userDto = response.getData();
        } catch (Exception e) {
            log.error("{} :: User not found for token validation: {}", getClass().getSimpleName(), e.getMessage());
            throw new ResourceNotFoundException("User", "id", userId);
        }

        UserDto userDtoResult = mapToUserDto(userDto);
        return ApiResponse.success("Token is valid", userDtoResult);
    }

    private void saveRefreshToken(String token, UserServiceUserDto user) {
        log.info("{} :: Saving refresh token for userId: {}", getClass().getSimpleName(), user.getId());
        try {
            SaveRefreshTokenRequest saveRequest = SaveRefreshTokenRequest.builder()
                    .token(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build();
            userClient.saveRefreshToken(saveRequest);
            log.info("{} :: Refresh token saved for userId: {}", getClass().getSimpleName(), user.getId());
        } catch (Exception e) {
            log.error("{} :: Error saving refresh token: {}", getClass().getSimpleName(), e.getMessage());
            throw new BusinessException("Error saving refresh token");
        }
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, UserServiceUserDto user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .user(mapToUserDto(user))
                .build();
    }

    private UserDto mapToUserDto(UserServiceUserDto user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
