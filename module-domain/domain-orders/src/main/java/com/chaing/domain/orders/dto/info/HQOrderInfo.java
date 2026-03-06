package com.chaing.domain.orders.dto.info;

import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.enums.HQOrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HQOrderInfo(
        Long orderId,

        String orderCode,

        HQOrderStatus status,

        String username,

        String phoneNumber,

        LocalDateTime requestedDate,

        LocalDateTime manufacturedDate,

        String storedDate,

        String description,

        Boolean isRegular
) {
    public static HQOrderInfo from(HeadOfficeOrder order) {
        return HQOrderInfo.builder()
                .orderId(order.getHeadOfficeOrderId())
                .orderCode(order.getOrderCode())
                .status(order.getOrderStatus())
                .username(order.getUsername())
                .phoneNumber(order.getPhoneNumber())
                .requestedDate(order.getCreatedAt())
                .manufacturedDate(order.getManufactureDate())
                .storedDate(order.getStoredDate())
                .description(order.getDescription())
                .isRegular(order.getIsRegular())
                .build();
    }
}