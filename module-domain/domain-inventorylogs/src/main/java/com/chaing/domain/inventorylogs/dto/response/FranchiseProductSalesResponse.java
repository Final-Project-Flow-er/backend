package com.chaing.domain.inventorylogs.dto.response;

import java.util.List;

public record FranchiseProductSalesResponse(
        Long franchiseId,
        List<ProductSalesResponse> products
) {
}
