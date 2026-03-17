package com.chaing.domain.orders.dto.info;

public record OrderInfoForLog(
        String orderCode,
        Long toId
) {
    public static OrderInfoForLog create(String orderCode, Long toId) {
        return new OrderInfoForLog(orderCode, toId);
    }
}
