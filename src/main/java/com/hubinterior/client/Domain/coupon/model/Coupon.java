package com.hubinterior.client.Domain.coupon.model;

import com.hubinterior.client.Domain.coupon.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_coupon", indexes = {
    @Index(name = "idx_coupon_code", columnList = "coupon_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_code", nullable = false, unique = true)
    private String couponCode;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    @Column(name = "discount_amount")
    private Float discountAmount;

    @Column(name = "min_order_value")
    private Float minOrderValue;

    @Column(name = "max_discount_cap")
    private Float maxDiscountCap;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;
}
