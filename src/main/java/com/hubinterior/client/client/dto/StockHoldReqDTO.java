package com.hubinterior.client.client.dto;

import java.util.List;

public record StockHoldReqDTO(
        String orderReference,
        List<HoldItem> items,
        int holdDurationMinutes
) {
    public record HoldItem(
            Long productId,
            int quantity
    ) {}
}
