package com.hubinterior.client.Domain.cart.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplyCouponReqDTO(
        @NotBlank(message = "Coupon code cannot be blank")
        String coupon_code
) {}
