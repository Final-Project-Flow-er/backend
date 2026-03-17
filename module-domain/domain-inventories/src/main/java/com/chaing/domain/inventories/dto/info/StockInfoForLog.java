package com.chaing.domain.inventories.dto.info;

public record StockInfoForLog(
        Long orderId,
        String boxCode,
        Long productId
) {
    public static StockInfoForLog create(Long orderId, String boxCode, Long productId){
        return new StockInfoForLog(orderId, boxCode, productId);
    }
}
