package com.chaing.domain.orders.dto.command;

import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record HQOrderItemCommand(
        Long orderItemId,

        Long productId,

        BigDecimal unitPrice,

        Integer quantity
) {
    public static HQOrderItemCommand from(HeadOfficeOrderItem headOfficeOrderItem) {
        return HQOrderItemCommand.builder()
                .orderItemId(headOfficeOrderItem.getHeadOfficeOrderItemId())
                .productId(headOfficeOrderItem.getProductId())
                .unitPrice(headOfficeOrderItem.getUnitPrice())
                .quantity(headOfficeOrderItem.getQuantity())
                .build();
    }
}
