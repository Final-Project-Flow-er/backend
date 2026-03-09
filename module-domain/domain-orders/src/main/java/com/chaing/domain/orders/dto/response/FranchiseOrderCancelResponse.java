package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;

public record FranchiseOrderCancelResponse(
        String orderCode,

        FranchiseOrderStatus status
) {
    public static FranchiseOrderCancelResponse from(FranchiseOrder order) {
        return new FranchiseOrderCancelResponse(
                order.getOrderCode(),
                order.getOrderStatus()
        );
    }
}
