package com.hubinterior.client.Domain.wishlist.dto;

import lombok.Builder;

@Builder
public record MoveToCartResponseDTO(
        Long client_id,
        int cart_total_items,
        int wishlist_total_items,
        Long cart_item_id,
        Long product_id
) {}
