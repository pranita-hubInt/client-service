package com.hubinterior.client.Domain.cart.repository;

import com.hubinterior.client.Domain.cart.model.Cart;
import com.hubinterior.client.Domain.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);
}
