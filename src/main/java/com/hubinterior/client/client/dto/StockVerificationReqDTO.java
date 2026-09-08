package com.hubinterior.client.client.dto;

import java.util.List;

public record StockVerificationReqDTO(
        List<StockItemReq> items
) {
    public record StockItemReq(
            Long productId,
            String skuId,
            int requestedQuantity
    ) {}
}
