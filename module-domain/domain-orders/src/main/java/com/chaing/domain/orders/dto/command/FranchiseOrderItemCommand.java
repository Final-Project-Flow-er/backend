package com.chaing.domain.orders.dto.command;

import com.chaing.domain.orders.entity.FranchiseOrderItem;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record FranchiseOrderItemCommand(
        Long orderItemId,

        Long productId,

        BigDecimal unitPrice
) {
    public static FranchiseOrderItemCommand from(FranchiseOrderItem franchiseOrderItem) {
        return FranchiseOrderItemCommand.builder()
                .orderItemId(franchiseOrderItem.getFranchiseOrderItemId())
                .productId(franchiseOrderItem.getProductId())
                .unitPrice(franchiseOrderItem.getUnitPrice())
                .build();
    }

    public static List<FranchiseOrderItemCommand> from(List<FranchiseOrderItem> franchiseOrderItems) {
        return franchiseOrderItems.stream()
                .map(FranchiseOrderItemCommand::from)
                .toList();
    }
}
