package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FranchiseOrderDetailResponse(
        String orderCode,

        FranchiseOrderStatus status,

        LocalDateTime requestedDate,

        String receiver,

        String phoneNumber,

        String address,

        LocalDateTime deliveryDate,

        String deliveryTime,

        List<FranchiseOrderItemDetailResponse> items
) {
}
