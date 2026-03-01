package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

@Builder
public record HQOrderStatusUpdateResponse(
        @NotBlank
        String orderCode,

        @NotBlank
        FranchiseOrderStatus status
) {
    public static HQOrderStatusUpdateResponse from(FranchiseOrder order) {
        return HQOrderStatusUpdateResponse.builder()
                .orderCode(order.getOrderCode())
                .status(order.getOrderStatus())
                .build();
    }

    public static List<HQOrderStatusUpdateResponse> from(List<FranchiseOrder> orders) {
        return orders.stream()
                .map(HQOrderStatusUpdateResponse::from)
                .toList();
    }
}
