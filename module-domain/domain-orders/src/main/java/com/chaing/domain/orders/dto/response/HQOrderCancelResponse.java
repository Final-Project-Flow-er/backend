package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.dto.command.HQOrderCancelCommand;
import com.chaing.domain.orders.enums.HQOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record HQOrderCancelResponse(
        String orderCode,

        HQOrderStatus status
) {
    public static HQOrderCancelResponse from(HQOrderCancelCommand cancelOrder) {
        return HQOrderCancelResponse.builder()
                .orderCode(cancelOrder.orderCode())
                .status(cancelOrder.status())
                .build();
    }
}
