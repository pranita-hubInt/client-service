package com.hubinterior.client.Domain.wishlist.dto;

import com.hubinterior.client.Domain.cart.dto.CartResponseDTO.PricingSummaryDTO;
import lombok.Builder;

@Builder
public record MoveToWishlistResponseDTO(
        Long cart_id,
        Long client_id,
        Long moved_product_id,
        int cart_total_items,
        int wishlist_total_items,
        PricingSummaryDTO pricing_summary
) {}
