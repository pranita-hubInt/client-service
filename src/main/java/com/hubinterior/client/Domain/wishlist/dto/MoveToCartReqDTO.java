package com.hubinterior.client.Domain.wishlist.dto;

import jakarta.validation.constraints.Min;

public record MoveToCartReqDTO(
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {}
