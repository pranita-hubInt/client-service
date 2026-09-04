package com.hubinterior.client.Domain.order.model;

import com.hubinterior.client.Domain.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client_order", indexes = {
    @Index(name = "idx_order_client", columnList = "client_id"),
    @Index(name = "idx_order_number", columnList = "order_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus status;

    @Column(name = "sub_total")
    private Float subTotal;

    @Column(name = "item_discount_total")
    private Float itemDiscountTotal;

    @Column(name = "coupon_discount")
    private Float couponDiscount;

    @Column(name = "applied_coupon_code")
    private String appliedCouponCode;

    @Column(name = "delivery_charges")
    private Float deliveryCharges;

    @Column(name = "total_tax")
    private Float totalTax;

    @Column(name = "net_payable_amount")
    private Float netPayableAmount;

    @Column(name = "hold_reference")
    private String holdReference;

    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
