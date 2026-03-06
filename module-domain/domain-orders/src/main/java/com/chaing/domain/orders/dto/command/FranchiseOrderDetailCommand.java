package com.chaing.domain.orders.dto.command;

import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record FranchiseOrderDetailCommand(
        Long orderId,

        String orderCode,

        FranchiseOrderStatus orderStatus,

        Integer quantity,

        BigDecimal totalPrice,

        LocalDateTime requestedDate,

        LocalDateTime deliveryDate,

        String deliveryTime,

        String address
) {
    public static FranchiseOrderDetailCommand from(FranchiseOrder order) {
        return FranchiseOrderDetailCommand.builder()
                .orderId(order.getFranchiseOrderId())
                .orderCode(order.getOrderCode())
                .orderStatus(order.getOrderStatus())
                .quantity(order.getTotalQuantity())
                .totalPrice(order.getTotalAmount())
                .requestedDate(order.getCreatedAt())
                .deliveryDate(order.getDeliveryDate())
                .deliveryTime(order.getDeliveryTime())
                .address(order.getAddress())
                .build();
    }
}
