package com.ecommerce.user.service;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.entity.RefreshToken;
import com.ecommerce.user.entity.UserCredential;
import com.ecommerce.user.exception.BusinessException;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.repository.RefreshTokenRepository;
import com.ecommerce.user.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserService {

    private final UserCredentialRepository userCredentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public ApiResponse<UserCredentialDto> registerUser(CreateUserCredentialRequest request) {
        log.info("{} :: Registering user with email: {}", getClass().getSimpleName(), request.getEmail());

        if (userCredentialRepository.existsByEmail(request.getEmail())) {
            log.warn("{} :: Email already registered: {}", getClass().getSimpleName(), request.getEmail());
            throw new BusinessException("Email already registered");
        }

        UserCredential credential = UserCredential.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : "ROLE_USER")
                .emailVerified(false)
                .enabled(true)
                .build();

        credential = userCredentialRepository.save(credential);
        log.info("{} :: User registered successfully with id: {} and email: {}",
                getClass().getSimpleName(), credential.getId(), credential.getEmail());

        return ApiResponse.success("User registered successfully", mapToDto(credential));
    }

    @Transactional
    public ApiResponse<UserCredentialDto> updateEmailVerified(Long userId, boolean emailVerified) {
        log.info("{} :: Updating emailVerified to {} for userId: {}", getClass().getSimpleName(), emailVerified, userId);

        UserCredential credential = userCredentialRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserCredential", "id", userId));

        credential.setEmailVerified(emailVerified);
        credential = userCredentialRepository.save(credential);

        log.info("{} :: emailVerified updated successfully for userId: {}", getClass().getSimpleName(), userId);
        return ApiResponse.success("Email verification status updated", mapToDto(credential));
    }

    public ApiResponse<UserCredentialDto> getUserByEmail(String email) {
        log.info("{} :: Fetching user by email: {}", getClass().getSimpleName(), email);

        UserCredential credential = userCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("UserCredential", "email", email));

        log.info("{} :: User found: {} with id: {}", getClass().getSimpleName(), email, credential.getId());
        return ApiResponse.success("User retrieved successfully", mapToDto(credential));
    }

    public ApiResponse<UserCredentialDto> getUserById(Long id) {
        log.info("{} :: Fetching user by id: {}", getClass().getSimpleName(), id);

        UserCredential credential = userCredentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserCredential", "id", id));

        log.info("{} :: User found with id: {}", getClass().getSimpleName(), id);
        return ApiResponse.success("User retrieved successfully", mapToDto(credential));
    }

    public ApiResponse<Boolean> checkEmailExists(String email) {
        log.info("{} :: Checking if email exists: {}", getClass().getSimpleName(), email);

        boolean exists = userCredentialRepository.existsByEmail(email);
        log.info("{} :: Email {} exists: {}", getClass().getSimpleName(), email, exists);

        return ApiResponse.success("Email check completed", exists);
    }

    @Transactional
    public ApiResponse<Void> updatePassword(Long userId, UpdatePasswordRequest request) {
        log.info("{} :: Updating password for userId: {}", getClass().getSimpleName(), userId);

        UserCredential credential = userCredentialRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserCredential", "id", userId));

        credential.setPassword(request.getNewPassword());
        userCredentialRepository.save(credential);

        log.info("{} :: Password updated successfully for userId: {}", getClass().getSimpleName(), userId);
        return ApiResponse.success("Password updated successfully");
    }

    @Transactional
    public ApiResponse<RefreshTokenDto> saveRefreshToken(SaveRefreshTokenRequest request) {
        log.info("{} :: Saving refresh token for userId: {}", getClass().getSimpleName(), request.getUserId());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(request.getToken())
                .userId(request.getUserId())
                .email(request.getEmail())
                .expiresAt(request.getExpiresAt())
                .revoked(false)
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);

        log.info("{} :: Refresh token saved for userId: {}", getClass().getSimpleName(), request.getUserId());
        return ApiResponse.success("Refresh token saved", mapToRefreshTokenDto(refreshToken));
    }

    public ApiResponse<RefreshTokenDto> getRefreshToken(String token) {
        log.info("{} :: Fetching refresh token", getClass().getSimpleName());

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("RefreshToken", "token", token));

        log.info("{} :: Refresh token found for userId: {}", getClass().getSimpleName(), refreshToken.getUserId());
        return ApiResponse.success("Refresh token retrieved", mapToRefreshTokenDto(refreshToken));
    }

    @Transactional
    public ApiResponse<RefreshTokenDto> updateRefreshToken(Long id, boolean revoked) {
        log.info("{} :: Updating refresh token id: {}, revoked: {}", getClass().getSimpleName(), id, revoked);

        RefreshToken refreshToken = refreshTokenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RefreshToken", "id", id));

        refreshToken.setRevoked(revoked);
        refreshToken = refreshTokenRepository.save(refreshToken);

        log.info("{} :: Refresh token updated for id: {}", getClass().getSimpleName(), id);
        return ApiResponse.success("Refresh token updated", mapToRefreshTokenDto(refreshToken));
    }

    @Transactional
    public ApiResponse<Void> deleteRefreshTokensByUserId(Long userId) {
        log.info("{} :: Deleting all refresh tokens for userId: {}", getClass().getSimpleName(), userId);

        refreshTokenRepository.deleteByUserId(userId);

        log.info("{} :: Refresh tokens deleted for userId: {}", getClass().getSimpleName(), userId);
        return ApiResponse.success("Refresh tokens deleted");
    }

    private UserCredentialDto mapToDto(UserCredential credential) {
        return UserCredentialDto.builder()
                .id(credential.getId())
                .email(credential.getEmail())
                .password(credential.getPassword())
                .name(credential.getName())
                .phone(credential.getPhone())
                .role(credential.getRole())
                .emailVerified(credential.isEmailVerified())
                .enabled(credential.isEnabled())
                .createdAt(credential.getCreatedAt())
                .updatedAt(credential.getUpdatedAt())
                .build();
    }

    private RefreshTokenDto mapToRefreshTokenDto(RefreshToken refreshToken) {
        return RefreshTokenDto.builder()
                .id(refreshToken.getId())
                .token(refreshToken.getToken())
                .userId(refreshToken.getUserId())
                .email(refreshToken.getEmail())
                .expiresAt(refreshToken.getExpiresAt())
                .createdAt(refreshToken.getCreatedAt())
                .revoked(refreshToken.isRevoked())
                .build();
    }
}
