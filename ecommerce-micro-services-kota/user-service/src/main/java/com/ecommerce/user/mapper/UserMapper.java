package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.AddressDto;
import com.ecommerce.user.dto.UserProfileDto;
import com.ecommerce.user.dto.WishlistItemDto;
import com.ecommerce.user.entity.Address;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.entity.WishlistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileDto toUserProfileDto(UserProfile userProfile);
    List<UserProfileDto> toUserProfileDtoList(List<UserProfile> userProfiles);

    AddressDto toAddressDto(Address address);
    List<AddressDto> toAddressDtoList(List<Address> addresses);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    Address toAddress(AddressDto addressDto);
    UserProfile toUserProfile(UserProfileDto userProfileDto);
    WishlistItemDto toWishlistItemDto(WishlistItem wishlistItem);
    List<WishlistItemDto> toWishlistItemDtoList(List<WishlistItem> wishlistItems);
}
