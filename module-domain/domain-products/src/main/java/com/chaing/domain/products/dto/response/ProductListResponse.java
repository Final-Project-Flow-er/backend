package com.chaing.domain.products.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductListResponse(
        List<ProductResponse> products
) {
}
