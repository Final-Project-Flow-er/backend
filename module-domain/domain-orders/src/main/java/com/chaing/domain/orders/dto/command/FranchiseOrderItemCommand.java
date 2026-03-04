package com.chaing.domain.orders.dto.command;

import com.chaing.domain.orders.entity.FranchiseOrderItem;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FranchiseOrderItemCommand(
        Long orderItemId,

        String serialCode,

        Long productId,

        BigDecimal unitPrice
) {
    public static FranchiseOrderItemCommand from(FranchiseOrderItem franchiseOrderItem) {
        return FranchiseOrderItemCommand.builder()
                .orderItemId(franchiseOrderItem.getFranchiseOrderItemId())
                .serialCode(franchiseOrderItem.getSerialCode())
                .productId(franchiseOrderItem.getProductId())
                .unitPrice(franchiseOrderItem.getUnitPrice())
                .build();
    }
}
