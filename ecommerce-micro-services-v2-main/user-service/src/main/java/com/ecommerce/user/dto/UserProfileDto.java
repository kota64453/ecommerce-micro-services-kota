package com.ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String avatarUrl;
    private List<AddressDto> addresses;
    private List<WishlistItemDto> wishlistItems;
}
