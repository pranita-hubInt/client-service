package com.hubinterior.client.Domain.order.controller;

import com.hubinterior.client.Common.ApiResponse;
import com.hubinterior.client.Domain.order.dto.CheckoutInitiateResDTO;
import com.hubinterior.client.Domain.order.dto.OrderResponseDTO;
import com.hubinterior.client.Domain.order.dto.PaymentConfirmReqDTO;
import com.hubinterior.client.Domain.order.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client/{client_id}/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<CheckoutInitiateResDTO>> initiateCheckout(
            @PathVariable("client_id") Long clientId
    ) {
        CheckoutInitiateResDTO response = checkoutService.initiateCheckout(clientId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Checkout session initiated with temporary inventory reservation", response));
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> confirmPayment(
            @PathVariable("client_id") Long clientId,
            @Valid @RequestBody PaymentConfirmReqDTO req
    ) {
        OrderResponseDTO response = checkoutService.confirmPayment(clientId, req);
        String message = "SUCCESS".equalsIgnoreCase(req.payment_status())
                ? "Payment confirmed and order placed successfully"
                : "Payment marked as failed and held stock released";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @PostMapping("/{order_id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> cancelCheckout(
            @PathVariable("client_id") Long clientId,
            @PathVariable("order_id") Long orderId
    ) {
        OrderResponseDTO response = checkoutService.cancelCheckout(clientId, orderId);
        return ResponseEntity.ok(ApiResponse.success("Checkout cancelled and reserved stock restored", response));
    }

    @GetMapping("/orders/{order_id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrder(
            @PathVariable("client_id") Long clientId,
            @PathVariable("order_id") Long orderId
    ) {
        OrderResponseDTO response = checkoutService.getOrder(clientId, orderId);
        return ResponseEntity.ok(ApiResponse.success("Order fetched successfully", response));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getClientOrders(
            @PathVariable("client_id") Long clientId
    ) {
        List<OrderResponseDTO> orders = checkoutService.getClientOrders(clientId);
        return ResponseEntity.ok(ApiResponse.success("Client orders retrieved successfully", orders));
    }

}

