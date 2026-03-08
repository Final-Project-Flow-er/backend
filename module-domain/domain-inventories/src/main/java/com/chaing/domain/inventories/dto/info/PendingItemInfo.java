package com.chaing.domain.inventories.dto.info;

import com.chaing.domain.inventories.dto.raw.InboundRawData;

import java.time.LocalDate;

public record PendingItemInfo(
        Long productId,
        String serialCode,
        LocalDate manufactureDate
) {
    public static PendingItemInfo fromItem(InboundRawData raw) {
        return new PendingItemInfo(raw.getProductId(), raw.getSerialCode(), raw.getManufactureDate());
    }
}
