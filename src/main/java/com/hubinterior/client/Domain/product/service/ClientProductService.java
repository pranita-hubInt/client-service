package com.hubinterior.client.Domain.product.service;

import com.hubinterior.client.Domain.product.dto.*;
import com.hubinterior.client.Exception.BusinessRuleException;
import com.hubinterior.client.Exception.ResourceNotFoundException;
import com.hubinterior.client.client.ProductCatalogFeignClient;
import com.hubinterior.client.client.dto.ProductSummaryInternalDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientProductService {

    private static final Logger log = LoggerFactory.getLogger(ClientProductService.class);
    private final ProductCatalogFeignClient productCatalogClient;

    public PageResponseDTO<ClientProductSummaryDTO> getAllProducts(int page, int size, String sort, String category, String search) {
        if (page < 0 || size <= 0) {
            throw new BusinessRuleException("Pagination parameters invalid: page must be >= 0 and size must be > 0.");
        }

        log.info("Fetching products for client storefront: page={}, size={}, sort={}, category={}, search={}",
                page, size, sort, category, search);

        PageResponseDTO<CoreProductResponseDTO> corePage = productCatalogClient.getAllProducts(page, size, sort);
        if (corePage == null || corePage.getContent() == null) {
            return PageResponseDTO.<ClientProductSummaryDTO>builder()
                    .content(Collections.emptyList())
                    .pageNumber(page)
                    .pageSize(size)
                    .totalElements(0)
                    .totalPages(0)
                    .empty(true)
                    .build();
        }

        List<ClientProductSummaryDTO> mappedItems = corePage.getContent().stream()
                .filter(item -> category == null || category.isBlank() || (item.category() != null && item.category().equalsIgnoreCase(category)))
                .filter(item -> search == null || search.isBlank() || matchesSearch(item, search))
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<ClientProductSummaryDTO>builder()
                .content(mappedItems)
                .pageNumber(corePage.getPageNumber())
                .pageSize(corePage.getPageSize())
                .totalElements(corePage.getTotalElements())
                .totalPages(corePage.getTotalPages())
                .first(corePage.isFirst())
                .last(corePage.isLast())
                .empty(mappedItems.isEmpty())
                .build();
    }

    public ClientProductDetailDTO getProductById(Long productId) {
        if (productId == null || productId <= 0) {
            throw new BusinessRuleException("Invalid product ID: " + productId);
        }

        log.info("Fetching client product details for ID: {}", productId);
        CoreProductResponseDTO coreProduct = productCatalogClient.getProductById(productId);

        if (coreProduct == null) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        // Fetch internal summary for real-time stock status
        boolean inStock = true;
        try {
            ProductSummaryInternalDTO summary = productCatalogClient.getProductSummary(productId);
            if (summary != null) {
                inStock = summary.is_in_stock();
            }
        } catch (Exception ignored) {
            // Stock query fallback
        }

        return toDetailDTO(coreProduct, inStock);
    }

    public List<ClientProductSummaryDTO> getFeaturedProducts(int limit) {
        if (limit <= 0) {
            throw new BusinessRuleException("Limit must be greater than zero.");
        }

        log.info("Fetching featured products for client storefront banner, limit={}", limit);
        PageResponseDTO<CoreProductResponseDTO> corePage = productCatalogClient.getAllProducts(0, Math.max(limit, 20), "prodId,desc");
        if (corePage == null || corePage.getContent() == null) {
            return Collections.emptyList();
        }

        return corePage.getContent().stream()
                .filter(CoreProductResponseDTO::featured_offer)
                .limit(limit)
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(CoreProductResponseDTO item, String query) {
        String lowerQuery = query.toLowerCase();
        if (item.offering_name() != null && item.offering_name().toLowerCase().contains(lowerQuery)) return true;
        if (item.brand() != null && item.brand().toLowerCase().contains(lowerQuery)) return true;
        if (item.category() != null && item.category().toLowerCase().contains(lowerQuery)) return true;
        if (item.tags() != null && item.tags().stream().anyMatch(t -> t.toLowerCase().contains(lowerQuery))) return true;
        return false;
    }

    private ClientProductSummaryDTO toSummaryDTO(CoreProductResponseDTO core) {
        Float sellingPrice = (core.pricing() != null && core.pricing().selling_price() != null)
                ? core.pricing().selling_price()
                : 0.0f;

        Integer discount = (core.pricing() != null && core.pricing().discount() != null)
                ? core.pricing().discount()
                : 0;

        float effectivePrice = discount > 0
                ? sellingPrice - (sellingPrice * discount / 100.0f)
                : sellingPrice;

        String units = (core.pricing() != null && core.pricing().units() != null)
                ? core.pricing().units()
                : "INR";

        return ClientProductSummaryDTO.builder()
                .productId(core.prodId())
                .offeringName(core.offering_name())
                .skuId(core.sku_id())
                .category(core.category())
                .brand(core.brand())
                .tags(core.tags())
                .shortDescription(core.short_desc())
                .isFeatured(core.featured_offer())
                .sellingPrice(sellingPrice)
                .discountPercentage(discount)
                .effectivePrice(effectivePrice)
                .currencyUnit(units)
                .build();
    }

    private ClientProductDetailDTO toDetailDTO(CoreProductResponseDTO core, boolean inStock) {
        Float sellingPrice = (core.pricing() != null && core.pricing().selling_price() != null)
                ? core.pricing().selling_price()
                : 0.0f;

        Integer discount = (core.pricing() != null && core.pricing().discount() != null)
                ? core.pricing().discount()
                : 0;

        float effectivePrice = discount > 0
                ? sellingPrice - (sellingPrice * discount / 100.0f)
                : sellingPrice;

        ClientProductDetailDTO.ClientPricingDTO pricingDTO = ClientProductDetailDTO.ClientPricingDTO.builder()
                .sellingPrice(sellingPrice)
                .discountPercentage(discount)
                .effectivePrice(effectivePrice)
                .gstRate(core.pricing() != null ? core.pricing().gst_rate() : null)
                .units(core.pricing() != null ? core.pricing().units() : "INR")
                .description(core.pricing() != null ? core.pricing().desc() : null)
                .build();

        return ClientProductDetailDTO.builder()
                .productId(core.prodId())
                .offeringName(core.offering_name())
                .offeringType(core.offering_type())
                .skuId(core.sku_id())
                .category(core.category())
                .brand(core.brand())
                .tags(core.tags())
                .shortDescription(core.short_desc())
                .isFeatured(core.featured_offer())
                .pricing(pricingDTO)
                .inStock(inStock)
                .build();
    }
}
