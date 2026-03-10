package com.chaing.domain.inventories.dto.info;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.raw.InboundRawData;

public record PendingBoxInfo(
        String boxCode,
        Long productId,
        Long countItem,
        Long orderId,
        LogType status
) {
    public static PendingBoxInfo fromBox(InboundRawData raw, Long countItem, Long orderId) {
        return new PendingBoxInfo(raw.getBoxCode(), raw.getProductId(), countItem, orderId, LogType.INBOUND_WAIT);
    }
}
