package com.chaing.domain.inventorylogs.dto.response;

import java.util.List;

public record ActorProductSalesResponse(
        Long actorId,
        List<ProductSalesResponse> products) {
}
