package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.dto.command.FranchiseOrderCommand;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import lombok.Builder;

@Builder
public record FranchiseOrderStatusShippingPendingResponse(
        String orderCode,

        FranchiseOrderStatus status
) {
    public static FranchiseOrderStatusShippingPendingResponse from(FranchiseOrderCommand franchiseOrderCommand) {
        return FranchiseOrderStatusShippingPendingResponse.builder()
                .orderCode(franchiseOrderCommand.orderCode())
                .status(franchiseOrderCommand.orderStatus())
                .build();
    }
}
