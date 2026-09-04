package com.hubinterior.client.Domain.order.dto;

import com.hubinterior.client.Domain.order.enums.OrderStatus;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponseDTO(
        Long order_id,
        String order_number,
        Long client_id,
        OrderStatus status,
        Float sub_total,
        Float item_discount_total,
        Float coupon_discount,
        String applied_coupon_code,
        Float delivery_charges,
        Float total_tax,
        Float net_payable_amount,
        String payment_transaction_id,
        List<OrderItemDTO> items,
        LocalDateTime created_at
) {
    @Builder
    public record OrderItemDTO(
            Long product_id,
            String sku_id,
            String product_name,
            int quantity,
            Float unit_price,
            Float total_price
    ) {}
}
