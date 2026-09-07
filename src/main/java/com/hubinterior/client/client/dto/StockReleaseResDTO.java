package com.hubinterior.client.client.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record StockReleaseResDTO(
        boolean success,
        String reference,
        String message,
        List<ReleasedItemDetail> releasedItems
) {
    @Builder
    public record ReleasedItemDetail(
            Long productId,
            String skuId,
            int releasedQuantity,
            int currentStock
    ) {}
}
