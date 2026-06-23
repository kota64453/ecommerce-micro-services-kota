package com.ecommerce.auth.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserCredentialRequest {
    private String email;
    private String password;
    private String name;
    private String phone;
    private String role;
}
