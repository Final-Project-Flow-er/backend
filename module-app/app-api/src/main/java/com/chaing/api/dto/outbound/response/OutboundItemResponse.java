package com.chaing.api.dto.outbound.response;

import java.time.LocalDate;

public record OutboundItemResponse(
        String serialCode,
        Long productId,
        String productName,
        LocalDate manufactureDate
) {
    public static OutboundItemResponse of(
            String serialCode,
            Long productId,
            String productName,
            LocalDate manufactureDate
    ) {
        return new OutboundItemResponse(serialCode, productId, productName, manufactureDate);
    }
}
