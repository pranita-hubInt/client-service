package com.hubinterior.client.client.dto;

import java.util.List;

public record StockReleaseReqDTO(
        String reference,
        List<ReleaseItem> items
) {
    public record ReleaseItem(
            Long productId,
            int quantity
    ) {}
}
