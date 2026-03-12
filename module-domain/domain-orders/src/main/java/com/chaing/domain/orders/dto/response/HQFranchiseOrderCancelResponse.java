package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import lombok.Builder;

import java.util.Map;

@Builder
public record HQFranchiseOrderCancelResponse(
        String orderCode,

        FranchiseOrderStatus status
) {
    public static HQFranchiseOrderCancelResponse of(Map.Entry<String, FranchiseOrderStatus> stringFranchiseOrderStatusEntry) {
        return HQFranchiseOrderCancelResponse.builder()
        .orderCode(stringFranchiseOrderStatusEntry.getKey())
        .status(stringFranchiseOrderStatusEntry.getValue())
        .build();
    }
}
