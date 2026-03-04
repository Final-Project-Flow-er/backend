package com.chaing.domain.orders.dto.command;

import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record FranchiseOrderCommand(
        Long orderId,

        String orderCode,

        FranchiseOrderStatus orderStatus,

        Integer quantity,

        BigDecimal totalPrice,

        LocalDateTime requestedDate,

        LocalDateTime deliveryDate
) {

    public static FranchiseOrderCommand from(FranchiseOrder franchiseOrder) {
        return FranchiseOrderCommand.builder()
                .orderId(franchiseOrder.getFranchiseOrderId())
                .orderCode(franchiseOrder.getOrderCode())
                .orderStatus(franchiseOrder.getOrderStatus())
                .quantity(franchiseOrder.getTotalQuantity())
                .totalPrice(franchiseOrder.getTotalAmount())
                .requestedDate(franchiseOrder.getCreatedAt())
                .build();
    }
}
