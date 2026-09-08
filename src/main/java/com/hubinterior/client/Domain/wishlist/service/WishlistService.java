package com.hubinterior.client.Domain.wishlist.service;

import com.hubinterior.client.Domain.cart.dto.CartItemReqDTO;
import com.hubinterior.client.Domain.cart.dto.CartResponseDTO;
import com.hubinterior.client.Domain.cart.service.CartService;
import com.hubinterior.client.Domain.wishlist.dto.MoveToCartResponseDTO;
import com.hubinterior.client.Domain.wishlist.dto.WishlistResponseDTO;
import com.hubinterior.client.Domain.wishlist.model.WishlistItem;
import com.hubinterior.client.Domain.wishlist.repository.WishlistItemRepository;
import com.hubinterior.client.Exception.DuplicateResourceException;
import com.hubinterior.client.Exception.ForbiddenException;
import com.hubinterior.client.Exception.ResourceNotFoundException;
import com.hubinterior.client.Exception.UnauthorizedException;
import com.hubinterior.client.client.ProductCatalogFeignClient;
import com.hubinterior.client.client.dto.ProductBatchReqDTO;
import com.hubinterior.client.client.dto.ProductSummaryInternalDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistRepo;
    private final ProductCatalogFeignClient productCatalogClient;

    @Lazy
    private final CartService cartService;

    @Transactional(readOnly = true)
    public WishlistResponseDTO getWishlist(Long clientId) {
        if (clientId == null || clientId <= 0) {
            throw new UnauthorizedException("Valid client ID / authentication context is required.");
        }

        List<WishlistItem> items = wishlistRepo.findByClientId(clientId);

        List<Long> productIds = items.stream()
                .map(WishlistItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, ProductSummaryInternalDTO> productMap = new HashMap<>();

        if (!productIds.isEmpty()) {
            try {
                List<ProductSummaryInternalDTO> summaries = productCatalogClient.getBatchProductSummary(
                        new ProductBatchReqDTO(productIds)
                );
                if (summaries != null) {
                    for (ProductSummaryInternalDTO s : summaries) {
                        productMap.put(s.prodId(), s);
                    }
                }
            } catch (Exception ignored) {}
        }

        List<WishlistResponseDTO.WishlistItemDetailDTO> itemDetails = items.stream().map(item -> {
            ProductSummaryInternalDTO prod = productMap.get(item.getProductId());
            float sellingPrice = prod != null && prod.selling_price() != null ? prod.selling_price() : 0.0f;
            float mrp = prod != null && prod.cost_price() != null ? prod.cost_price() : sellingPrice;
            int stock = prod != null ? prod.current_stock() : 0;
            boolean inStock = prod != null && prod.is_in_stock() && stock > 0;

            return WishlistResponseDTO.WishlistItemDetailDTO.builder()
                    .wishlist_item_id(item.getId())
                    .product_id(item.getProductId())
                    .sku_id(prod != null ? prod.sku_id() : item.getSkuId())
                    .product_name(prod != null ? prod.offering_name() : "Product #" + item.getProductId())
                    .primary_image_url(prod != null ? prod.primary_image_url() : null)
                    .unit_mrp(mrp)
                    .unit_selling_price(sellingPrice)
                    .in_stock(inStock)
                    .available_stock(stock)
                    .build();
        }).collect(Collectors.toList());

        return WishlistResponseDTO.builder()
                .client_id(clientId)
                .total_items_count(itemDetails.size())
                .items(itemDetails)
                .build();
    }

    @Transactional
    public void addToWishlist(Long clientId, Long productId, String skuId) {
        if (clientId == null || clientId <= 0) {
            throw new UnauthorizedException("Valid client ID / authentication context is required.");
        }

        // 1. Check duplicate
        Optional<WishlistItem> existingOpt = wishlistRepo.findByClientIdAndProductId(clientId, productId);
        if (existingOpt.isPresent()) {
            throw new DuplicateResourceException("Product with ID " + productId + " is already present in your wishlist.");
        }

        // 2. Check product exists in catalog
        ProductSummaryInternalDTO prod = productCatalogClient.getProductSummary(productId);
        if (prod == null) {
            throw new ResourceNotFoundException("Product with ID " + productId + " does not exist in catalog.");
        }

        wishlistRepo.save(WishlistItem.builder()
                .clientId(clientId)
                .productId(productId)
                .skuId(skuId != null ? skuId : prod.sku_id())
                .build());
    }

    @Transactional
    public MoveToCartResponseDTO moveToCart(Long clientId, Long wishlistItemId, int quantity) {
        if (clientId == null || clientId <= 0) {
            throw new UnauthorizedException("Valid client ID / authentication context is required.");
        }

        Optional<WishlistItem> itemOpt = wishlistRepo.findById(wishlistItemId);
        if (itemOpt.isEmpty()) {
            throw new ResourceNotFoundException("Wishlist item not found with id: " + wishlistItemId);
        }

        WishlistItem wishlistItem = itemOpt.get();
        if (!wishlistItem.getClientId().equals(clientId)) {
            throw new ForbiddenException("Access denied: You do not have permission to modify another user's wishlist item.");
        }

        Long productId = wishlistItem.getProductId();

        CartResponseDTO updatedCart = cartService.addItemToCart(clientId, new CartItemReqDTO(productId, quantity));
        wishlistRepo.delete(wishlistItem);

        Long cartItemId = updatedCart.items().stream()
                .filter(i -> i.product_id().equals(productId))
                .map(CartResponseDTO.CartItemDetailDTO::cart_item_id)
                .findFirst()
                .orElse(null);

        int wishlistCount = getWishlistCount(clientId);

        return MoveToCartResponseDTO.builder()
                .client_id(clientId)
                .cart_total_items(updatedCart.total_items_count())
                .wishlist_total_items(wishlistCount)
                .cart_item_id(cartItemId)
                .product_id(productId)
                .build();
    }

    @Transactional
    public void removeFromWishlist(Long clientId, Long wishlistItemId) {
        if (clientId == null || clientId <= 0) {
            throw new UnauthorizedException("Valid client ID / authentication context is required.");
        }

        Optional<WishlistItem> itemOpt = wishlistRepo.findById(wishlistItemId);
        if (itemOpt.isEmpty()) {
            throw new ResourceNotFoundException("Wishlist item not found with id: " + wishlistItemId);
        }

        WishlistItem item = itemOpt.get();
        if (!item.getClientId().equals(clientId)) {
            throw new ForbiddenException("Access denied: You do not have permission to remove another user's wishlist item.");
        }

        wishlistRepo.delete(item);
    }

    @Transactional(readOnly = true)
    public int getWishlistCount(Long clientId) {
        return (int) wishlistRepo.countByClientId(clientId);
    }
}
