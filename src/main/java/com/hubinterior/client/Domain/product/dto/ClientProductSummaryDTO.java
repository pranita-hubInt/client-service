package com.hubinterior.client.Domain.product.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ClientProductSummaryDTO(
        Long productId,
        String offeringName,
        String skuId,
        String category,
        String brand,
        List<String> tags,
        String shortDescription,
        boolean isFeatured,
        Float sellingPrice,
        Integer discountPercentage,
        Float effectivePrice,
        String currencyUnit
) {}
