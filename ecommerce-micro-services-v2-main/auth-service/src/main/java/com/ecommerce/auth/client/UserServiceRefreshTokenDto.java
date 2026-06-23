package com.ecommerce.auth.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserServiceRefreshTokenDto {
    private Long id;
    private String token;
    private Long userId;
    private String email;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean revoked;
}
