package com.hubinterior.client.Domain.cart.service;

import com.hubinterior.client.Domain.cart.dto.CartItemReqDTO;
import com.hubinterior.client.Domain.cart.dto.CartItemUpdateReqDTO;
import com.hubinterior.client.Domain.cart.dto.CartResponseDTO;
import com.hubinterior.client.Domain.cart.model.Cart;
import com.hubinterior.client.Domain.cart.model.CartItem;
import com.hubinterior.client.Domain.cart.repository.CartItemRepository;
import com.hubinterior.client.Domain.cart.repository.CartRepository;
import com.hubinterior.client.Domain.coupon.model.Coupon;
import com.hubinterior.client.Domain.coupon.service.CouponService;
import com.hubinterior.client.Domain.wishlist.dto.MoveToWishlistResponseDTO;
import com.hubinterior.client.Domain.wishlist.model.WishlistItem;
import com.hubinterior.client.Domain.wishlist.repository.WishlistItemRepository;
import com.hubinterior.client.Exception.BusinessRuleException;
import com.hubinterior.client.Exception.ResourceNotFoundException;
import com.hubinterior.client.client.ProductCatalogFeignClient;
import com.hubinterior.client.client.dto.ProductBatchReqDTO;
import com.hubinterior.client.client.dto.ProductSummaryInternalDTO;
import com.hubinterior.client.client.dto.StockVerificationReqDTO;
import com.hubinterior.client.client.dto.StockVerificationResDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final WishlistItemRepository wishlistRepo;
    private final PricingEngine pricingEngine;
    private final CouponService couponService;
    private final ProductCatalogFeignClient productCatalogClient;

    @Transactional
    public Cart getOrCreateCart(Long clientId) {
        return cartRepo.findByClientId(clientId)
                .orElseGet(() -> cartRepo.save(Cart.builder()
                        .clientId(clientId)
                        .items(new ArrayList<>())
                        .build()));
    }

    @Transactional(readOnly = true)
    public CartResponseDTO getCartResponse(Long clientId) {
        Cart cart = getOrCreateCart(clientId);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponseDTO addItemToCart(Long clientId, CartItemReqDTO req) {
        try {
            StockVerificationResDTO stockRes = productCatalogClient.verifyStock(
                    new StockVerificationReqDTO(List.of(
                            new StockVerificationReqDTO.StockItemReq(req.product_id(), null, req.quantity())
                    ))
            );

            if (stockRes != null && !stockRes.all_available()) {
                String errorMsg = stockRes.item_statuses().isEmpty()
                        ? "Product is out of stock"
                        : stockRes.item_statuses().get(0).message();
                throw new BusinessRuleException(errorMsg, "INSUFFICIENT_STOCK");
            }
        } catch (BusinessRuleException ex) {
            throw ex;
        } catch (Exception ignored) {}

        ProductSummaryInternalDTO productSummary = null;
        try {
            productSummary = productCatalogClient.getProductSummary(req.product_id());
        } catch (Exception ignored) {}

        Cart cart = getOrCreateCart(clientId);

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(req.product_id()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + req.quantity());
            if (productSummary != null && productSummary.selling_price() != null) {
                item.setAddedPrice(productSummary.selling_price());
            }
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(req.product_id())
                    .skuId(productSummary != null ? productSummary.sku_id() : "SKU-" + req.product_id())
                    .quantity(req.quantity())
                    .addedPrice(productSummary != null ? productSummary.selling_price() : 0.0f)
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepo.save(cart);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponseDTO updateItemQuantity(Long clientId, Long cartItemId, CartItemUpdateReqDTO req) {
        Cart cart = getOrCreateCart(clientId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (req.quantity() <= 0) {
            cart.getItems().remove(item);
            cartItemRepo.delete(item);
        } else {
            item.setQuantity(req.quantity());
        }

        cartRepo.save(cart);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponseDTO removeItemFromCart(Long clientId, Long cartItemId) {
        Cart cart = getOrCreateCart(clientId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        cart.getItems().remove(item);
        cartItemRepo.delete(item);
        cartRepo.save(cart);

        return buildCartResponse(cart);
    }

    @Transactional
    public MoveToWishlistResponseDTO moveToWishlist(Long clientId, Long cartItemId) {
        Cart cart = getOrCreateCart(clientId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        Long movedProductId = item.getProductId();
        String skuId = item.getSkuId();

        // 1. Add to wishlist
        if (wishlistRepo.findByClientIdAndProductId(clientId, movedProductId).isEmpty()) {
            wishlistRepo.save(WishlistItem.builder()
                    .clientId(clientId)
                    .productId(movedProductId)
                    .skuId(skuId)
                    .build());
        }

        // 2. Remove from cart
        cart.getItems().remove(item);
        cartItemRepo.delete(item);
        cartRepo.save(cart);

        CartResponseDTO updatedCart = buildCartResponse(cart);
        int wishlistCount = (int) wishlistRepo.countByClientId(clientId);

        return MoveToWishlistResponseDTO.builder()
                .cart_id(cart.getId())
                .client_id(clientId)
                .moved_product_id(movedProductId)
                .cart_total_items(updatedCart.total_items_count())
                .wishlist_total_items(wishlistCount)
                .pricing_summary(updatedCart.pricing_summary())
                .build();
    }

    @Transactional
    public CartResponseDTO applyCoupon(Long clientId, String couponCode) {
        Cart cart = getOrCreateCart(clientId);
        Coupon coupon = couponService.getValidCoupon(couponCode);

        CartResponseDTO currentSummary = buildCartResponse(cart);
        if (coupon.getMinOrderValue() != null && currentSummary.pricing_summary().sub_total() < coupon.getMinOrderValue()) {
            throw new BusinessRuleException("Cart subtotal of " + currentSummary.pricing_summary().sub_total() +
                    " does not meet the minimum order value of " + coupon.getMinOrderValue() + " for coupon " + couponCode);
        }

        cart.setAppliedCouponCode(coupon.getCouponCode());
        cartRepo.save(cart);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponseDTO removeCoupon(Long clientId) {
        Cart cart = getOrCreateCart(clientId);
        cart.setAppliedCouponCode(null);
        cartRepo.save(cart);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponseDTO clearCart(Long clientId) {
        Cart cart = getOrCreateCart(clientId);
        cart.getItems().clear();
        cart.setAppliedCouponCode(null);
        cartRepo.save(cart);

        return buildCartResponse(cart);
    }

    public CartResponseDTO buildCartResponse(Cart cart) {
        List<Long> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
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

        Coupon coupon = null;
        if (cart.getAppliedCouponCode() != null) {
            coupon = couponService.findByCode(cart.getAppliedCouponCode()).orElse(null);
        }

        PricingEngine.CartCalculationResult result = pricingEngine.calculateCart(cart.getItems(), productMap, coupon);

        return CartResponseDTO.builder()
                .cart_id(cart.getId())
                .client_id(cart.getClientId())
                .customer_email("client" + cart.getClientId() + "@example.com")
                .total_items_count(result.totalItemsCount())
                .items(result.items())
                .pricing_summary(result.pricingSummary())
                .build();
    }
}