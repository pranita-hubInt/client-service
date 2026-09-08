package com.hubinterior.client.client;

import com.hubinterior.client.Domain.product.dto.CoreProductResponseDTO;
import com.hubinterior.client.Domain.product.dto.PageResponseDTO;
import com.hubinterior.client.client.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "core-product-service", url = "${services.core.url:http://localhost:8080}")
public interface ProductCatalogFeignClient {

    @GetMapping("/api/v1/products/getAllProducts")
    PageResponseDTO<CoreProductResponseDTO> getAllProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", required = false) String sort
    );

    @GetMapping("/api/v1/products/getProduct/{prod_id}")
    CoreProductResponseDTO getProductById(@PathVariable("prod_id") Long prodId);

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
