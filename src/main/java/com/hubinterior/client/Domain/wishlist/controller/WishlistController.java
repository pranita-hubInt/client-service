package com.hubinterior.client.Domain.wishlist.controller;

import com.hubinterior.client.Common.ApiResponse;
import com.hubinterior.client.Domain.wishlist.dto.MoveToCartReqDTO;
import com.hubinterior.client.Domain.wishlist.dto.MoveToCartResponseDTO;
import com.hubinterior.client.Domain.wishlist.dto.WishlistResponseDTO;
import com.hubinterior.client.Domain.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/client/{client_id}/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<WishlistResponseDTO>> getWishlist(
            @PathVariable("client_id") Long clientId
    ) {
        WishlistResponseDTO response = wishlistService.getWishlist(clientId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist fetched successfully", response));
    }

    @PostMapping("/items/{wishlist_item_id}/move-to-cart")
    public ResponseEntity<ApiResponse<MoveToCartResponseDTO>> moveToCart(
            @PathVariable("client_id") Long clientId,
            @PathVariable("wishlist_item_id") Long wishlistItemId,
            @Valid @RequestBody(required = false) MoveToCartReqDTO req
    ) {
        int quantity = (req != null && req.quantity() > 0) ? req.quantity() : 1;
        MoveToCartResponseDTO response = wishlistService.moveToCart(clientId, wishlistItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Item moved from wishlist to cart successfully", response));
    }

    @DeleteMapping("/items/{wishlist_item_id}")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(
            @PathVariable("client_id") Long clientId,
            @PathVariable("wishlist_item_id") Long wishlistItemId
    ) {
        wishlistService.removeFromWishlist(clientId, wishlistItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from wishlist successfully", "Removed"));
    }
}
