package com.chaing.domain.orders.dto.response;

import java.util.List;

// 운송 시 필요한 발주 정보 dto
public record HQOrderForTransitResponse(
        Long orderId,
        String orderCode,
        List<OrderItemForTransit> items
        ) {
    public record OrderItemForTransit(
            Long productId,
            Integer quantity
    ) {}

    public static HQOrderForTransitResponse of(Long orderId, String orderCode, List<OrderItemForTransit> items) {
        return new HQOrderForTransitResponse(orderId, orderCode, items);
    }
}
