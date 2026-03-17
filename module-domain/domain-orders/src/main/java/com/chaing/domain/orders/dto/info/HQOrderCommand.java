package com.chaing.domain.orders.dto.info;

import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.enums.HQOrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HQOrderCommand(
        Long orderId,

        String orderCode,

        HQOrderStatus status,

        Long userId,

        LocalDateTime requestedDate,

        LocalDateTime manufacturedDate,

        String storedDate,

        String description,

        Boolean isRegular
) {
    public static HQOrderCommand from(HeadOfficeOrder order) {
        return HQOrderCommand.builder()
                .orderId(order.getHeadOfficeOrderId())
                .orderCode(order.getOrderCode())
                .status(order.getOrderStatus())
                .userId(order.getUserId())
                .requestedDate(order.getCreatedAt())
                .manufacturedDate(order.getManufactureDate())
                .storedDate(order.getStoredDate())
                .description(order.getDescription())
                .isRegular(order.getIsRegular())
                .build();
    }
}