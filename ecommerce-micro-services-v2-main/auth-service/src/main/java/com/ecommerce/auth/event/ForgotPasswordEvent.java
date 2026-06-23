package com.ecommerce.auth.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordEvent {
    private String email;
    private String otp;
    private String name;
    private String eventType;
}
