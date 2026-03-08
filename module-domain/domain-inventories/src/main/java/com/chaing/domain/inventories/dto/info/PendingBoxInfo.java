package com.chaing.domain.inventories.dto.info;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.raw.InboundRawData;

public record PendingBoxInfo(
        String boxCode,
        Long productId,
        LogType status
) {
    public static PendingBoxInfo fromBox(InboundRawData raw) {
        return new PendingBoxInfo(raw.getBoxCode(), raw.getProductId(), LogType.INBOUND_WAIT);
    }
}
