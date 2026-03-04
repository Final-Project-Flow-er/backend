package com.chaing.api.dto.franchise.orders.response;

import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record FranchiseOrderResponse(
        String orderCode,

        FranchiseOrderStatus orderStatus,

        String productCode,

        BigDecimal unitPrice,

        BigDecimal totalPrice,

        LocalDateTime requestedDate,

        String receiver,

        LocalDateTime deliveryDate
) {
}
