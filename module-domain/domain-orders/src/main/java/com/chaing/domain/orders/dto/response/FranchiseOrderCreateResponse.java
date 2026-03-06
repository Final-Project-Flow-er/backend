package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FranchiseOrderCreateResponse(
        String orderCode,

        FranchiseOrderStatus orderStatus,

        BigDecimal totalPrice,

        LocalDateTime requestedDate,

        String receiver,

        LocalDateTime deliveryDate,

        List<FranchiseOrderItemDetailResponse> items
) {
}
