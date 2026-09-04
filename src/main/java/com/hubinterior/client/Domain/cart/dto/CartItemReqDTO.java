package com.hubinterior.client.Domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemReqDTO(
        @NotNull(message = "Product ID is required")
        Long product_id,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {}
