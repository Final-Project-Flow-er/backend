package com.chaing.api.dto.inbound.response;

public record InboundBoxSummaryResponse(
        String boxCode,
        String orderCode,
        String productCode,
        String productName,
        Long countItem
) {
    public static InboundBoxSummaryResponse of(
            String boxCode, String orderCode, String productCode, String productName, Long countItem) {
        return new InboundBoxSummaryResponse(boxCode, orderCode, productCode, productName, countItem);
    }
}
