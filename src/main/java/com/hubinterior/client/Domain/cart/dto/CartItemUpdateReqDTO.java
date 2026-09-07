package com.hubinterior.client.Domain.cart.dto;

import jakarta.validation.constraints.Min;

public record CartItemUpdateReqDTO(
        @Min(value = 0, message = "Quantity cannot be negative")
        int quantity
) {}
