package com.hubinterior.client.client.dto;

import lombok.Builder;

@Builder
public record ProductSummaryInternalDTO(
        Long prodId,
        String sku_id,
        String offering_name,
        String brand,
        Float selling_price,
        Float cost_price,
        Integer discount,
        String gst_rate,
        int current_stock,
        String primary_image_url,
        boolean is_in_stock
) {}
