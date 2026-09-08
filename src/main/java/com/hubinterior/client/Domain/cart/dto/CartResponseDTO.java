package com.hubinterior.client.Domain.cart.dto;

import com.hubinterior.client.Domain.coupon.enums.DiscountType;
import lombok.Builder;
import java.util.List;

@Builder
public record CartResponseDTO(
        Long cart_id,
        Long client_id,
        String customer_email,
        int total_items_count,
        List<CartItemDetailDTO> items,
        PricingSummaryDTO pricing_summary
) {
    @Builder
    public record CartItemDetailDTO(
            Long cart_item_id,
            Long product_id,
            String sku_id,
            String product_name,
            String primary_image_url,
            Float unit_mrp,
            Float unit_selling_price,
            int quantity,
            Float item_subtotal,
            Float item_discount,
            boolean in_stock,
            int available_stock
    ) {}

    @Builder
    public record PricingSummaryDTO(
            Float mrp_total,
            Float item_discount_total,
            Float sub_total,
            CouponDetailDTO coupon,
            Float coupon_discount,
            Float delivery_charges,
            GstBreakdownDTO gst_breakdown,
            Float net_payable_amount
    ) {}

    @Builder
    public record CouponDetailDTO(
            String coupon_code,
            boolean is_active,
            DiscountType discount_type,
            Integer discount_percentage,
            Float discount_amount,
            Float min_order_value,
            Float max_discount_cap
    ) {}

    @Builder
    public record GstBreakdownDTO(
            Float cgst_amount,
            Float sgst_amount,
            Float total_tax
    ) {}
}
