package com.hubinterior.client.Domain.product.controller;

import com.hubinterior.client.Common.ApiResponse;
import com.hubinterior.client.Domain.product.dto.ClientProductDetailDTO;
import com.hubinterior.client.Domain.product.dto.ClientProductSummaryDTO;
import com.hubinterior.client.Domain.product.dto.PageResponseDTO;
import com.hubinterior.client.Domain.product.service.ClientProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client/products")
@RequiredArgsConstructor
public class ClientProductController {

    private final ClientProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<ClientProductSummaryDTO>>> getAllProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "prodId,asc") String sort,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search
    ) {
        PageResponseDTO<ClientProductSummaryDTO> products = productService.getAllProducts(page, size, sort, category, search);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ClientProductDetailDTO>> getProductById(
            @PathVariable Long productId
    ) {
        ClientProductDetailDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success("Product details retrieved successfully", product));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ClientProductSummaryDTO>>> getFeaturedProducts(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        List<ClientProductSummaryDTO> featured = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(ApiResponse.success("Featured products retrieved successfully", featured));
    }
}
