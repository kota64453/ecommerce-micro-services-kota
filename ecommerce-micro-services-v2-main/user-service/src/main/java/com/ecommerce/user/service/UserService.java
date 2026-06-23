package com.ecommerce.user.service;

import com.ecommerce.user.dto.AddressDto;
import com.ecommerce.user.dto.ApiResponse;
import com.ecommerce.user.dto.UserProfileDto;
import com.ecommerce.user.dto.WishlistItemDto;
import com.ecommerce.user.entity.Address;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.entity.WishlistItem;
import com.ecommerce.user.exception.BusinessException;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.repository.UserProfileRepository;
import com.ecommerce.user.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final AddressRepository addressRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserMapper userMapper;

    @Transactional
   // public ApiResponse<UserProfileDto> createProfile(UserProfile userProfile) {
        //log.info("{} :: Creating profile for userId: {}, email: {}", getClass().getSimpleName(), userProfile.getId(), userProfile.getEmail());

        //if (userProfileRepository.existsByEmail(userProfile.getEmail())) {
            //log.warn("{} :: Profile already exists for email: {}", getClass().getSimpleName(), email);
          //  throw new BusinessException("Profile already exists for this email");
        //}

        //userProfile = userProfileRepository.save(userProfile);
        //log.info("{} :: User profile created for: {}", getClass().getSimpleName(), userProfile.getEmail());
      //  return ApiResponse.success("Profile created successfully", userMapper.toUserProfileDto(userProfile));
    //}
    public ApiResponse<UserProfileDto> createProfile(UserProfileDto profileDto) {
        log.info("{} :: Creating profile for userId: {}, email: {}", getClass().getSimpleName(), profileDto.getId(), profileDto.getEmail());

        if (userProfileRepository.existsByEmail(profileDto.getEmail())) {
            log.warn("{} :: Profile already exists for email: {}", getClass().getSimpleName(), profileDto.getEmail()); // ✅ fixed
            throw new BusinessException("Profile already exists for this email");
        }

        UserProfile userProfile = userMapper.toUserProfile(profileDto);
        userProfile = userProfileRepository.save(userProfile);
        log.info("{} :: User profile created for: {}", getClass().getSimpleName(), userProfile.getEmail());
        return ApiResponse.success("Profile created successfully", userMapper.toUserProfileDto(userProfile));
    }

    public ApiResponse<UserProfileDto> getProfile(Long userId) {
        log.info("{} :: Fetching profile for userId: {}", getClass().getSimpleName(), userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("{} :: Profile not found for userId: {}", getClass().getSimpleName(), userId);
                    return new ResourceNotFoundException("User profile", "id", userId);
                });

        log.info("{} :: Profile retrieved for userId: {}", getClass().getSimpleName(), userId);
        return ApiResponse.success("Profile retrieved successfully", userMapper.toUserProfileDto(profile));
    }

    @Transactional
    public ApiResponse<UserProfileDto> updateProfile(Long userId, UserProfileDto profileDto) {
        log.info("{} :: Updating profile for userId: {}", getClass().getSimpleName(), userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("{} :: Profile not found for userId: {}", getClass().getSimpleName(), userId);
                    return new ResourceNotFoundException("User profile", "id", userId);
                });

        if (profileDto.getName() != null) profile.setName(profileDto.getName());
        if (profileDto.getPhone() != null) profile.setPhone(profileDto.getPhone());
        if (profileDto.getAvatarUrl() != null) profile.setAvatarUrl(profileDto.getAvatarUrl());

        profile = userProfileRepository.save(profile);
        log.info("{} :: User profile updated for userId: {}", getClass().getSimpleName(), userId);
        return ApiResponse.success("Profile updated successfully", userMapper.toUserProfileDto(profile));
    }

    public ApiResponse<UserProfileDto> getProfileByEmail(String email) {
        log.info("{} :: Fetching profile by email: {}", getClass().getSimpleName(), email);

        UserProfile profile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("{} :: Profile not found for email: {}", getClass().getSimpleName(), email);
                    return new ResourceNotFoundException("User profile", "email", email);
                });

        log.info("{} :: Profile retrieved for email: {}", getClass().getSimpleName(), email);
        return ApiResponse.success("Profile retrieved successfully", userMapper.toUserProfileDto(profile));
    }

    // Address Management

    @Transactional
    public ApiResponse<AddressDto> addAddress(Long userId, AddressDto addressDto) {
        log.info("{} :: Adding address for userId: {}", getClass().getSimpleName(), userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("{} :: Profile not found for userId: {}", getClass().getSimpleName(), userId);
                    return new ResourceNotFoundException("User profile", "id", userId);
                });

        Address address = userMapper.toAddress(addressDto);
        address.setUser(profile);

        if (address.isDefault() || profile.getAddresses().isEmpty()) {
            profile.getAddresses().forEach(a -> a.setDefault(false));
        }

        address = addressRepository.save(address);
        log.info("{} :: Address added for userId: {}", getClass().getSimpleName(), userId);
        return ApiResponse.success("Address added successfully", userMapper.toAddressDto(address));
    }

    public ApiResponse<List<AddressDto>> getAddresses(Long userId) {
        log.info("{} :: Fetching addresses for userId: {}", getClass().getSimpleName(), userId);

        List<Address> addresses = addressRepository.findByUserId(userId);
        log.info("{} :: Found {} addresses for userId: {}", getClass().getSimpleName(), addresses.size(), userId);
        return ApiResponse.success("Addresses retrieved successfully", userMapper.toAddressDtoList(addresses));
    }

    @Transactional
    public ApiResponse<AddressDto> updateAddress(Long userId, Long addressId, AddressDto addressDto) {
        log.info("{} :: Updating address {} for userId: {}", getClass().getSimpleName(), addressId, userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("{} :: Address not found: {}", getClass().getSimpleName(), addressId);
                    return new ResourceNotFoundException("Address", "id", addressId);
                });

        if (!address.getUser().getId().equals(userId)) {
            log.warn("{} :: Address {} does not belong to userId: {}", getClass().getSimpleName(), addressId, userId);
            throw new BusinessException("Address does not belong to this user");
        }

        if (addressDto.getStreet() != null) address.setStreet(addressDto.getStreet());
        if (addressDto.getCity() != null) address.setCity(addressDto.getCity());
        if (addressDto.getState() != null) address.setState(addressDto.getState());
        if (addressDto.getZipCode() != null) address.setZipCode(addressDto.getZipCode());
        if (addressDto.getCountry() != null) address.setCountry(addressDto.getCountry());
        if (addressDto.getLabel() != null) address.setLabel(addressDto.getLabel());
        if (addressDto.getPhone() != null) address.setPhone(addressDto.getPhone());

        if (addressDto.isDefault() && !address.isDefault()) {
            UserProfile profile = address.getUser();
            profile.getAddresses().forEach(a -> a.setDefault(false));
            address.setDefault(true);
        }

        address = addressRepository.save(address);
        log.info("{} :: Address updated for userId: {}", getClass().getSimpleName(), userId);
        return ApiResponse.success("Address updated successfully", userMapper.toAddressDto(address));
    }

    @Transactional
    public ApiResponse<Void> deleteAddress(Long userId, Long addressId) {
        log.info("{} :: Deleting address {} for userId: {}", getClass().getSimpleName(), addressId, userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("{} :: Address not found: {}", getClass().getSimpleName(), addressId);
                    return new ResourceNotFoundException("Address", "id", addressId);
                });

        if (!address.getUser().getId().equals(userId)) {
            log.warn("{} :: Address {} does not belong to userId: {}", getClass().getSimpleName(), addressId, userId);
            throw new BusinessException("Address does not belong to this user");
        }

        addressRepository.delete(address);
        log.info("{} :: Address deleted for userId: {}", getClass().getSimpleName(), userId);
        return ApiResponse.success("Address deleted successfully");
    }

    // Wishlist Management

    public ApiResponse<List<WishlistItemDto>> getWishlist(Long userId) {
        log.info("{} :: Fetching wishlist for userId: {}", getClass().getSimpleName(), userId);

        List<WishlistItem> items = wishlistItemRepository.findByUserId(userId);
        log.info("{} :: Found {} wishlist items for userId: {}", getClass().getSimpleName(), items.size(), userId);
        return ApiResponse.success("Wishlist retrieved successfully", userMapper.toWishlistItemDtoList(items));
    }

    @Transactional
    public ApiResponse<WishlistItemDto> addToWishlist(Long userId, String productId) {
        log.info("{} :: Adding product {} to wishlist for userId: {}", getClass().getSimpleName(), productId, userId);

        if (wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            log.warn("{} :: Product {} already in wishlist for userId: {}", getClass().getSimpleName(), productId, userId);
            throw new BusinessException("Product already in wishlist");
        }

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("{} :: Profile not found for userId: {}", getClass().getSimpleName(), userId);
                    return new ResourceNotFoundException("User profile", "id", userId);
                });

        WishlistItem item = WishlistItem.builder()
                .user(profile)
                .productId(productId)
                .build();

        item = wishlistItemRepository.save(item);
        log.info("{} :: Product {} added to wishlist for userId: {}", getClass().getSimpleName(), productId, userId);
        return ApiResponse.success("Product added to wishlist", userMapper.toWishlistItemDto(item));
    }

    @Transactional
    public ApiResponse<Void> removeFromWishlist(Long userId, String productId) {
        log.info("{} :: Removing product {} from wishlist for userId: {}", getClass().getSimpleName(), productId, userId);

        WishlistItem item = wishlistItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> {
                    log.warn("{} :: Wishlist item not found for product: {}, userId: {}", getClass().getSimpleName(), productId, userId);
                    return new ResourceNotFoundException("Wishlist item", "productId", productId);
                });
        wishlistItemRepository.delete(item);
        log.info("{} :: Product {} removed from wishlist for userId: {}", getClass().getSimpleName(), productId, userId);
        return ApiResponse.success("Product removed from wishlist");
    }
}
