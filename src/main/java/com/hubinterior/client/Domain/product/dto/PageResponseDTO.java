package com.hubinterior.client.Domain.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponseDTO<T> {

    @Builder.Default
    private List<T> content = new ArrayList<>();

    @JsonProperty("number")
    private int pageNumber;

    @JsonProperty("size")
    private int pageSize;

    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private boolean empty;
}
