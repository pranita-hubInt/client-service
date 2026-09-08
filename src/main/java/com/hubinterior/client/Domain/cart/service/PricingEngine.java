package com.hubinterior.client.Domain.cart.service;

import com.hubinterior.client.Domain.cart.dto.CartResponseDTO.*;
import com.hubinterior.client.Domain.cart.model.CartItem;
import com.hubinterior.client.Domain.coupon.enums.DiscountType;
import com.hubinterior.client.Domain.coupon.model.Coupon;
import com.hubinterior.client.client.dto.ProductSummaryInternalDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PricingEngine {

    private static final float FREE_SHIPPING_THRESHOLD = 50000.0f;
    private static final float STANDARD_SHIPPING_FEE = 500.0f;
    private static final float DEFAULT_GST_RATE = 0.18f;

    public CartCalculationResult calculateCart(
            List<CartItem> cartItems,
            Map<Long, ProductSummaryInternalDTO> productMap,
            Coupon coupon
    ) {
        float mrpTotal = 0.0f;
        float itemDiscountTotal = 0.0f;
        float subTotal = 0.0f;
        int totalItemsCount = 0;

        List<CartItemDetailDTO> itemDetails = new ArrayList<>();

        for (CartItem item : cartItems) {
            ProductSummaryInternalDTO prod = productMap.get(item.getProductId());

            float sellingPrice = prod != null && prod.selling_price() != null
                    ? prod.selling_price()
                    : (item.getAddedPrice() != null ? item.getAddedPrice() : 0.0f);

            float mrp = prod != null && prod.cost_price() != null && prod.cost_price() > sellingPrice
                    ? prod.cost_price()
                    : sellingPrice;

            int qty = item.getQuantity();
            totalItemsCount += qty;

            float itemSubtotal = sellingPrice * qty;
            float unitDiscount = Math.max(0.0f, mrp - sellingPrice);
            float itemDiscount = unitDiscount * qty;

            mrpTotal += (mrp * qty);
            itemDiscountTotal += itemDiscount;
            subTotal += itemSubtotal;

            int availableStock = prod != null ? prod.current_stock() : 0;
            boolean inStock = prod != null && prod.is_in_stock() && availableStock >= qty;

            itemDetails.add(CartItemDetailDTO.builder()
                    .cart_item_id(item.getId())
                    .product_id(item.getProductId())
                    .sku_id(prod != null ? prod.sku_id() : item.getSkuId())
                    .product_name(prod != null ? prod.offering_name() : "Product #" + item.getProductId())
                    .primary_image_url(prod != null ? prod.primary_image_url() : null)
                    .unit_mrp(mrp)
                    .unit_selling_price(sellingPrice)
                    .quantity(qty)
                    .item_subtotal(itemSubtotal)
                    .item_discount(itemDiscount)
                    .in_stock(inStock)
                    .available_stock(availableStock)
                    .build());
        }

        // Coupon calculation
        float couponDiscount = 0.0f;
        CouponDetailDTO couponDetail = null;

        if (coupon != null && coupon.isActive() && subTotal > 0) {
            float minOrder = coupon.getMinOrderValue() != null ? coupon.getMinOrderValue() : 0.0f;

            if (subTotal >= minOrder) {
                if (coupon.getDiscountType() == DiscountType.PERCENTAGE && coupon.getDiscountPercentage() != null) {
                    float calculatedDiscount = subTotal * (coupon.getDiscountPercentage() / 100.0f);
                    if (coupon.getMaxDiscountCap() != null && calculatedDiscount > coupon.getMaxDiscountCap()) {
                        couponDiscount = coupon.getMaxDiscountCap();
                    } else {
                        couponDiscount = calculatedDiscount;
                    }
                } else if (coupon.getDiscountType() == DiscountType.FLAT && coupon.getDiscountAmount() != null) {
                    couponDiscount = Math.min(subTotal, coupon.getDiscountAmount());
                }

                couponDetail = CouponDetailDTO.builder()
                        .coupon_code(coupon.getCouponCode())
                        .is_active(coupon.isActive())
                        .discount_type(coupon.getDiscountType())
                        .discount_percentage(coupon.getDiscountPercentage())
                        .discount_amount(couponDiscount)
                        .min_order_value(coupon.getMinOrderValue())
                        .max_discount_cap(coupon.getMaxDiscountCap())
                        .build();
            }
        }

        // Delivery charges
        float deliveryCharges = (subTotal > 0 && subTotal < FREE_SHIPPING_THRESHOLD) ? STANDARD_SHIPPING_FEE : 0.0f;

        // GST breakdown
        float taxableValue = Math.max(0.0f, subTotal - couponDiscount);
        float totalTax = taxableValue * DEFAULT_GST_RATE;
        float cgst = totalTax / 2.0f;
        float sgst = totalTax / 2.0f;

        GstBreakdownDTO gstBreakdown = GstBreakdownDTO.builder()
                .cgst_amount(cgst)
                .sgst_amount(sgst)
                .total_tax(totalTax)
                .build();

        float netPayable = Math.max(0.0f, subTotal - couponDiscount + deliveryCharges);

        PricingSummaryDTO pricingSummary = PricingSummaryDTO.builder()
                .mrp_total(mrpTotal)
                .item_discount_total(itemDiscountTotal)
                .sub_total(subTotal)
                .coupon(couponDetail)
                .coupon_discount(couponDiscount)
                .delivery_charges(deliveryCharges)
                .gst_breakdown(gstBreakdown)
                .net_payable_amount(netPayable)
                .build();

        return new CartCalculationResult(totalItemsCount, itemDetails, pricingSummary);
    }

    public record CartCalculationResult(
            int totalItemsCount,
            List<CartItemDetailDTO> items,
            PricingSummaryDTO pricingSummary
    ) {}
}
