package com.hubinterior.client.Domain.order.repository;

import com.hubinterior.client.Domain.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByClientIdOrderByCreatedAtDesc(Long clientId);
    Optional<Order> findByIdAndClientId(Long id, Long clientId);
}
