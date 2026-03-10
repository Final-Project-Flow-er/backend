package com.chaing.api.dto.inbound.response;

import java.time.LocalDate;

public record InboundDetailResponse(
        String serialCode,
        Long productId,
        String productCode,
        String productName,
        LocalDate manufactureDate) {
    public static InboundDetailResponse of(
            String serialCode,
            Long productId,
            String productCode,
            String productName,
            LocalDate manufactureDate) {
        return new InboundDetailResponse(serialCode, productId, productCode, productName, manufactureDate);
    }
}
