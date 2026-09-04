package com.hubinterior.client.Domain.order.dto;

import com.hubinterior.client.Domain.cart.dto.CartResponseDTO.PricingSummaryDTO;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record CheckoutInitiateResDTO(
        Long order_id,
        String order_number,
        Long client_id,
        String status,
        String hold_reference,
        LocalDateTime hold_expires_at,
        PricingSummaryDTO pricing_summary,
        String payment_gateway_url
) {}
