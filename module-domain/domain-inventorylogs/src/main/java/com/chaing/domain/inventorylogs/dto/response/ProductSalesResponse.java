package com.chaing.domain.inventorylogs.dto.response;

import java.util.List;

public record ProductSalesResponse(
        Long productId,
        List<DailySales> sales,
        Integer totalSales
) {
}
