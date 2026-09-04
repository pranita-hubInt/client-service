package com.hubinterior.client.client;

import com.hubinterior.client.client.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "core-product-service", url = "${services.core.url:http://localhost:8080}")
public interface ProductCatalogFeignClient {

    @PostMapping("/api/v1/products/internal/batch-summary")
    List<ProductSummaryInternalDTO> getBatchProductSummary(@RequestBody ProductBatchReqDTO request);

    @GetMapping("/api/v1/products/internal/{prodId}/summary")
    ProductSummaryInternalDTO getProductSummary(@PathVariable("prodId") Long prodId);

    @PostMapping("/api/v1/inventory/internal/verify-stock")
    StockVerificationResDTO verifyStock(@RequestBody StockVerificationReqDTO request);

    @PostMapping("/api/v1/inventory/internal/hold-stock")
    StockHoldResDTO holdStock(@RequestBody StockHoldReqDTO request);

    @PostMapping("/api/v1/inventory/internal/release-stock")
    StockReleaseResDTO releaseStock(@RequestBody StockReleaseReqDTO request);

    @PostMapping("/api/v1/inventory/internal/deduct-stock")
    StockDeductionResDTO deductStock(@RequestBody StockDeductionReqDTO request);
}
