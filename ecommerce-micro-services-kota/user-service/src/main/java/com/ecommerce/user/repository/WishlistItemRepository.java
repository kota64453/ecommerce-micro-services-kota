package com.ecommerce.user.repository;

import com.ecommerce.user.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserId(Long userId);
    Optional<WishlistItem> findByUserIdAndProductId(Long userId, String productId);
    boolean existsByUserIdAndProductId(Long userId, String productId);
    void deleteByUserIdAndProductId(Long userId, String productId);
}
