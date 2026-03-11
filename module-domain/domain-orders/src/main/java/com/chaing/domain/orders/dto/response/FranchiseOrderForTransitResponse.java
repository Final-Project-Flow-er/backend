package com.chaing.domain.orders.dto.response;

import java.time.LocalDateTime;
import java.util.List;

// 운송 시 필요한 발주 정보 dto
public record FranchiseOrderForTransitResponse(
        Long orderId,
        String orderCode,
        List<OrderItemForTransit> items,
        Long franchiseId,
        LocalDateTime orderCreatedAt,
        LocalDateTime deliveryDate
        ) {
    public record OrderItemForTransit(
            Long productId,
            Integer quantity
    ) {}

    public static FranchiseOrderForTransitResponse of(Long orderId, String orderCode, List<OrderItemForTransit> items, Long franchiseId, LocalDateTime orderCreatedAt, LocalDateTime deliveryDate) {
        return new FranchiseOrderForTransitResponse(orderId, orderCode, items, franchiseId, orderCreatedAt, deliveryDate);
    }
}
