package com.chaing.api.dto.hq.products.response;

import lombok.Builder;

import java.util.List;

@Builder
public record HQProductListResponse(
        List<HQProductResponse> HQProductList
) {
}
