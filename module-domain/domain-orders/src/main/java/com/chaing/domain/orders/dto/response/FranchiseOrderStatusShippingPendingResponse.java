package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.FranchiseOrderStatus;

public record FranchiseOrderStatusShippingPendingResponse(
        String orderCode,

        FranchiseOrderStatus status
) {
}
