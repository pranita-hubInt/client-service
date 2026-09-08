package com.hubinterior.client.Domain.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record CoreProductResponseDTO(
        Long prodId,
        String offering_name,
        String offering_type,
        PricingDTO pricing,
        String sku_id,
        String category,
        String brand,
        List<String> tags,
        String short_desc,
        boolean featured_offer
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    public record PricingDTO(
            Float cost_price,
            Float selling_price,
            Integer discount,
            String gst_rate,
            String units,
            Integer margin_percentage,
            String desc
    ) {}
}
