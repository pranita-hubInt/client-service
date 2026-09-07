package com.hubinterior.client.Domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentConfirmReqDTO(
        @NotNull(message = "Order ID is required")
        Long order_id,

        @NotBlank(message = "Payment transaction ID is required")
        String transaction_id,

        @NotBlank(message = "Payment status is required (SUCCESS / FAILED)")
        String payment_status
) {}
