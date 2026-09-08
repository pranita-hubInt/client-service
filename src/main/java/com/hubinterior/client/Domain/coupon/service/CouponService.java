package com.hubinterior.client.Domain.coupon.service;

import com.hubinterior.client.Domain.coupon.enums.DiscountType;
import com.hubinterior.client.Domain.coupon.model.Coupon;
import com.hubinterior.client.Domain.coupon.repository.CouponRepository;
import com.hubinterior.client.Exception.BusinessRuleException;
import com.hubinterior.client.Exception.DuplicateResourceException;
import com.hubinterior.client.Exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepo;

    @PostConstruct
    public void initSampleCoupons() {
        if (couponRepo.count() == 0) {
            couponRepo.save(Coupon.builder()
                    .couponCode("FESTIVE15")
                    .isActive(true)
                    .discountType(DiscountType.PERCENTAGE)
                    .discountPercentage(15)
                    .minOrderValue(20000.0f)
                    .maxDiscountCap(6000.0f)
                    .validUntil(LocalDateTime.now().plusMonths(3))
                    .build());

            couponRepo.save(Coupon.builder()
                    .couponCode("MERRY10")
                    .isActive(true)
                    .discountType(DiscountType.PERCENTAGE)
                    .discountPercentage(10)
                    .minOrderValue(10000.0f)
                    .maxDiscountCap(5000.0f)
                    .validUntil(LocalDateTime.now().plusMonths(3))
                    .build());

            couponRepo.save(Coupon.builder()
                    .couponCode("HOMES5000")
                    .isActive(true)
                    .discountType(DiscountType.FLAT)
                    .discountAmount(5000.0f)
                    .minOrderValue(50000.0f)
                    .validUntil(LocalDateTime.now().plusMonths(3))
                    .build());
        }
    }

    public Coupon getValidCoupon(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            throw new BusinessRuleException("Coupon code cannot be null or empty.");
        }

        Coupon coupon = couponRepo.findByCouponCodeIgnoreCase(couponCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon '" + couponCode + "' not found"));

        if (!coupon.isActive()) {
            throw new BusinessRuleException("Coupon '" + couponCode + "' is currently inactive or disabled", "COUPON_INACTIVE");
        }

        if (coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Coupon '" + couponCode + "' has expired on " + coupon.getValidUntil(), "COUPON_EXPIRED");
        }

        return coupon;
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        if (coupon.getCouponCode() == null || coupon.getCouponCode().trim().isEmpty()) {
            throw new BusinessRuleException("Coupon code is mandatory.");
        }

        if (couponRepo.findByCouponCodeIgnoreCase(coupon.getCouponCode().trim()).isPresent()) {
            throw new DuplicateResourceException("Coupon code '" + coupon.getCouponCode() + "' already exists.");
        }

        return couponRepo.save(coupon);
    }

    public Optional<Coupon> findByCode(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return Optional.empty();
        }
        return couponRepo.findByCouponCodeIgnoreCase(couponCode.trim());
    }
}
