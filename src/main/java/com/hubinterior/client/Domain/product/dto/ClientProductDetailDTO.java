package com.hubinterior.client.Domain.product.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ClientProductDetailDTO(
        Long productId,
        String offeringName,
        String offeringType,
        String skuId,
        String category,
        String brand,
        List<String> tags,
        String shortDescription,
        boolean isFeatured,
        ClientPricingDTO pricing,
        boolean inStock
) {
    @Builder
    public record ClientPricingDTO(
            Float sellingPrice,
            Integer discountPercentage,
            Float effectivePrice,
            String gstRate,
            String units,
            String description
    ) {}
}
