package com.chaing.api.dto.franchise.products.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FranchiseProductListResponse(
    List<FranchiseProductResponse> franchiseProductList
) {
}
