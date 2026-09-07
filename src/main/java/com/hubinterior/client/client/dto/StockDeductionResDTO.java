package com.hubinterior.client.client.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record StockDeductionResDTO(
        boolean success,
        String orderReference,
        String message,
        List<DeductedItemDetail> deductedItems,
        List<String> lowStockAlerts
) {
    @Builder
    public record DeductedItemDetail(
            Long productId,
            String skuId,
            int deductedQuantity,
            int remainingStock,
            boolean lowStockTriggered
    ) {}
}
