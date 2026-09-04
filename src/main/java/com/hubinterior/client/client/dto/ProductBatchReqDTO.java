package com.hubinterior.client.client.dto;

import java.util.List;

public record ProductBatchReqDTO(
        List<Long> productIds
) {}
