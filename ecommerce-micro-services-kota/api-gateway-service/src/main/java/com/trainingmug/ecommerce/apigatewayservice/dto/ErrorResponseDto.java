package com.trainingmug.ecommerce.apigatewayservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class ErrorResponseDto {
    private boolean success;

    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;
}
