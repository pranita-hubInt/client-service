package com.hubinterior.client.client.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record StockVerificationResDTO(
        boolean all_available,
        List<StockItemStatus> item_statuses
) {
    @Builder
    public record StockItemStatus(
            Long productId,
            String skuId,
            int requestedQuantity,
            int availableStock,
            boolean isSufficient,
            String message
    ) {}
}
