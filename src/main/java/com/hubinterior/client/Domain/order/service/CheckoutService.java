package com.hubinterior.client.Domain.order.service;

import com.hubinterior.client.Domain.cart.dto.CartResponseDTO;
import com.hubinterior.client.Domain.cart.model.Cart;
import com.hubinterior.client.Domain.cart.model.CartItem;
import com.hubinterior.client.Domain.cart.service.CartService;
import com.hubinterior.client.Domain.order.dto.CheckoutInitiateResDTO;
import com.hubinterior.client.Domain.order.dto.OrderResponseDTO;
import com.hubinterior.client.Domain.order.dto.PaymentConfirmReqDTO;
import com.hubinterior.client.Domain.order.enums.OrderStatus;
import com.hubinterior.client.Domain.order.model.Order;
import com.hubinterior.client.Domain.order.model.OrderItem;
import com.hubinterior.client.Domain.order.repository.OrderRepository;
import com.hubinterior.client.Exception.BusinessRuleException;
import com.hubinterior.client.Exception.ResourceNotFoundException;
import com.hubinterior.client.client.ProductCatalogFeignClient;
import com.hubinterior.client.client.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final OrderRepository orderRepo;
    private final CartService cartService;
    private final ProductCatalogFeignClient productCatalogClient;

    @Transactional
    public CheckoutInitiateResDTO initiateCheckout(Long clientId) {
        Cart cart = cartService.getOrCreateCart(clientId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Cannot initiate checkout on an empty cart");
        }

        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + clientId;

        List<StockHoldReqDTO.HoldItem> holdItems = cart.getItems().stream()
                .map(i -> new StockHoldReqDTO.HoldItem(i.getProductId(), i.getQuantity()))
                .collect(Collectors.toList());

        StockHoldResDTO holdRes;
        try {
            holdRes = productCatalogClient.holdStock(new StockHoldReqDTO(orderNumber, holdItems, 15));
        } catch (Exception ex) {
            throw new BusinessRuleException("Failed to reach Core Service for inventory reservation: " + ex.getMessage());
        }

        if (holdRes == null || !holdRes.success()) {
            String error = (holdRes != null && holdRes.message() != null) ? holdRes.message() : "Insufficient stock to complete checkout";
            throw new BusinessRuleException(error, "INSUFFICIENT_STOCK");
        }

        CartResponseDTO cartSummary = cartService.getCartResponse(clientId);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .clientId(clientId)
                .status(OrderStatus.PENDING_PAYMENT)
                .subTotal(cartSummary.pricing_summary().sub_total())
                .itemDiscountTotal(cartSummary.pricing_summary().item_discount_total())
                .couponDiscount(cartSummary.pricing_summary().coupon_discount())
                .appliedCouponCode(cart.getAppliedCouponCode())
                .deliveryCharges(cartSummary.pricing_summary().delivery_charges())
                .totalTax(cartSummary.pricing_summary().gst_breakdown() != null ? cartSummary.pricing_summary().gst_breakdown().total_tax() : 0.0f)
                .netPayableAmount(cartSummary.pricing_summary().net_payable_amount())
                .holdReference(holdRes.holdReference())
                .items(new ArrayList<>())
                .build();

        for (CartItem ci : cart.getItems()) {
            float unitPrice = ci.getAddedPrice() != null ? ci.getAddedPrice() : 0.0f;
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .productId(ci.getProductId())
                    .skuId(ci.getSkuId())
                    .productName("Product #" + ci.getProductId())
                    .quantity(ci.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(unitPrice * ci.getQuantity())
                    .build();
            order.getItems().add(oi);
        }

        Order savedOrder = orderRepo.save(order);

        return CheckoutInitiateResDTO.builder()
                .order_id(savedOrder.getId())
                .order_number(savedOrder.getOrderNumber())
                .client_id(clientId)
                .status(savedOrder.getStatus().name())
                .hold_reference(savedOrder.getHoldReference())
                .hold_expires_at(holdRes.expiresAt())
                .pricing_summary(cartSummary.pricing_summary())
                .payment_gateway_url("https://pay.homesandmerry.com/gateway/session/" + savedOrder.getOrderNumber())
                .build();
    }

    @Transactional
    public OrderResponseDTO confirmPayment(Long clientId, PaymentConfirmReqDTO req) {
        Order order = orderRepo.findByIdAndClientId(req.order_id(), clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + req.order_id()));

        if (order.getStatus() == OrderStatus.PAYMENT_COMPLETED) {
            return mapToOrderResponse(order);
        }

        if ("SUCCESS".equalsIgnoreCase(req.payment_status())) {
            List<StockDeductionReqDTO.DeductionItem> deductionItems = order.getItems().stream()
                    .map(i -> new StockDeductionReqDTO.DeductionItem(i.getProductId(), i.getQuantity()))
                    .collect(Collectors.toList());

            try {
                productCatalogClient.deductStock(new StockDeductionReqDTO(order.getOrderNumber(), deductionItems));
            } catch (Exception ignored) {}

            order.setStatus(OrderStatus.PAYMENT_COMPLETED);
            order.setPaymentTransactionId(req.transaction_id());
            orderRepo.save(order);

            cartService.clearCart(clientId);
        } else {
            releaseOrderHeldStock(order);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            order.setPaymentTransactionId(req.transaction_id());
            orderRepo.save(order);
        }

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponseDTO cancelCheckout(Long clientId, Long orderId) {
        Order order = orderRepo.findByIdAndClientId(orderId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            releaseOrderHeldStock(order);
            order.setStatus(OrderStatus.CANCELLED);
            orderRepo.save(order);
        }

        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(Long clientId, Long orderId) {
        Order order = orderRepo.findByIdAndClientId(orderId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToOrderResponse(order);
    }

    private void releaseOrderHeldStock(Order order) {
        if (order.getHoldReference() != null && !order.getItems().isEmpty()) {
            List<StockReleaseReqDTO.ReleaseItem> releaseItems = order.getItems().stream()
                    .map(i -> new StockReleaseReqDTO.ReleaseItem(i.getProductId(), i.getQuantity()))
                    .collect(Collectors.toList());
            try {
                productCatalogClient.releaseStock(new StockReleaseReqDTO(order.getHoldReference(), releaseItems));
            } catch (Exception ignored) {}
        }
    }

    private OrderResponseDTO mapToOrderResponse(Order order) {
        List<OrderResponseDTO.OrderItemDTO> items = order.getItems().stream()
                .map(i -> OrderResponseDTO.OrderItemDTO.builder()
                        .product_id(i.getProductId())
                        .sku_id(i.getSkuId())
                        .product_name(i.getProductName())
                        .quantity(i.getQuantity())
                        .unit_price(i.getUnitPrice())
                        .total_price(i.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .order_id(order.getId())
                .order_number(order.getOrderNumber())
                .client_id(order.getClientId())
                .status(order.getStatus())
                .sub_total(order.getSubTotal())
                .item_discount_total(order.getItemDiscountTotal())
                .coupon_discount(order.getCouponDiscount())
                .applied_coupon_code(order.getAppliedCouponCode())
                .delivery_charges(order.getDeliveryCharges())
                .total_tax(order.getTotalTax())
                .net_payable_amount(order.getNetPayableAmount())
                .payment_transaction_id(order.getPaymentTransactionId())
                .items(items)
                .created_at(order.getCreatedAt())
                .build();
    }
}
