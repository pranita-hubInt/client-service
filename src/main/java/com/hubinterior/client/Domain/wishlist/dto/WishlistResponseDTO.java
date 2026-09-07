package com.hubinterior.client.Domain.wishlist.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record WishlistResponseDTO(
        Long client_id,
        int total_items_count,
        List<WishlistItemDetailDTO> items
) {
    @Builder
    public record WishlistItemDetailDTO(
            Long wishlist_item_id,
            Long product_id,
            String sku_id,
            String product_name,
            String primary_image_url,
            Float unit_mrp,
            Float unit_selling_price,
            boolean in_stock,
            int available_stock
    ) {}
}
