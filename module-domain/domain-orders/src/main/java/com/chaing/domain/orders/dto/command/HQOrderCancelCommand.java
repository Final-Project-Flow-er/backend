package com.chaing.domain.orders.dto.command;

import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.enums.HQOrderStatus;
import lombok.Builder;

@Builder
public record HQOrderCancelCommand(
        String orderCode,

        HQOrderStatus status
) {
    public static HQOrderCancelCommand from(HeadOfficeOrder order) {
        return HQOrderCancelCommand.builder()
                .orderCode(order.getOrderCode())
                .status(order.getOrderStatus())
                .build();
    }
}
