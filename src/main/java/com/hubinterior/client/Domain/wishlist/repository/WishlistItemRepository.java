package com.hubinterior.client.Domain.wishlist.repository;

import com.hubinterior.client.Domain.wishlist.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByClientId(Long clientId);
    Optional<WishlistItem> findByClientIdAndProductId(Long clientId, Long productId);
    long countByClientId(Long clientId);
}
