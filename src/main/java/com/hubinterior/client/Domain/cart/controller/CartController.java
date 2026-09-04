package com.hubinterior.client.Domain.cart.controller;

import com.hubinterior.client.Common.ApiResponse;
import com.hubinterior.client.Domain.cart.dto.ApplyCouponReqDTO;
import com.hubinterior.client.Domain.cart.dto.CartItemReqDTO;
import com.hubinterior.client.Domain.cart.dto.CartItemUpdateReqDTO;
import com.hubinterior.client.Domain.cart.dto.CartResponseDTO;
import com.hubinterior.client.Domain.cart.service.CartService;
import com.hubinterior.client.Domain.wishlist.dto.MoveToWishlistResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/client/{client_id}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponseDTO>> addItemToCart(
            @PathVariable("client_id") Long clientId,
            @Valid @RequestBody CartItemReqDTO req
    ) {
        CartResponseDTO response = cartService.addItemToCart(clientId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart successfully", response));
    }

    @PutMapping("/items/{cart_item_id}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> updateItemQuantity(
            @PathVariable("client_id") Long clientId,
            @PathVariable("cart_item_id") Long cartItemId,
            @Valid @RequestBody CartItemUpdateReqDTO req
    ) {
        CartResponseDTO response = cartService.updateItemQuantity(clientId, cartItemId, req);
        return ResponseEntity.ok(ApiResponse.success("Cart item quantity updated", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart(
            @PathVariable("client_id") Long clientId
    ) {
        CartResponseDTO response = cartService.getCartResponse(clientId);
        return ResponseEntity.ok(ApiResponse.success("Cart fetched successfully", response));
    }

    @DeleteMapping("/items/{cart_item_id}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> removeItemFromCart(
            @PathVariable("client_id") Long clientId,
            @PathVariable("cart_item_id") Long cartItemId
    ) {
        CartResponseDTO response = cartService.removeItemFromCart(clientId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", response));
    }

    @PostMapping("/items/{cart_item_id}/move-to-wishlist")
    public ResponseEntity<ApiResponse<MoveToWishlistResponseDTO>> moveToWishlist(
            @PathVariable("client_id") Long clientId,
            @PathVariable("cart_item_id") Long cartItemId
    ) {
        MoveToWishlistResponseDTO response = cartService.moveToWishlist(clientId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item moved from cart to wishlist successfully", response));
    }

    @PostMapping("/coupons/apply")
    public ResponseEntity<ApiResponse<CartResponseDTO>> applyCoupon(
            @PathVariable("client_id") Long clientId,
            @Valid @RequestBody ApplyCouponReqDTO req
    ) {
        CartResponseDTO response = cartService.applyCoupon(clientId, req.coupon_code());
        String msg = "Coupon " + req.coupon_code() + " applied successfully";
        if (response.pricing_summary().coupon_discount() != null && response.pricing_summary().coupon_discount() > 0) {
            msg += ". You saved ₹" + response.pricing_summary().coupon_discount() + "!";
        }
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @DeleteMapping("/coupons/remove")
    public ResponseEntity<ApiResponse<CartResponseDTO>> removeCoupon(
            @PathVariable("client_id") Long clientId
    ) {
        CartResponseDTO response = cartService.removeCoupon(clientId);
        return ResponseEntity.ok(ApiResponse.success("Coupon removed successfully", response));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartResponseDTO>> clearCart(
            @PathVariable("client_id") Long clientId
    ) {
        CartResponseDTO response = cartService.clearCart(clientId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", response));
    }
}
