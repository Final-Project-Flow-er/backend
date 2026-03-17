package com.chaing.domain.transports.dto;

import java.time.LocalDateTime;

public record OrderInfo(
        Long orderId,      // 주문 고유 ID
        String orderCode,  // 발주 번호
        Long weight,        // 적재량/무게
        Long franchiseId,
        LocalDateTime orderCreatedAt,
        LocalDateTime deliveryDate
) {
}