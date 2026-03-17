package com.chaing.domain.orders.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FranchiseOrderUpdateResponse(
        String orderCode,

        String cancelReason,

        List<FranchiseOrderItemDetailResponse> items
) {
}
