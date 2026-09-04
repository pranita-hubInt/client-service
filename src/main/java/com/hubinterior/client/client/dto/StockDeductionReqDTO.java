package com.hubinterior.client.client.dto;

import java.util.List;

public record StockDeductionReqDTO(
        String orderReference,
        List<DeductionItem> items
) {
    public record DeductionItem(
            Long productId,
            int quantity
    ) {}
}
