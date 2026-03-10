package com.chaing.api.dto.outbound.response;

public record OutboundBoxSummaryResponse(
        String boxCode,
        String orderCode,
        String productCode,
        String productName,
        String franchiseName,
        Long countItem
) {
    public static OutboundBoxSummaryResponse of(
            String boxCode, String orderCode, String productCode, String productName, String franchiseName, Long countItem
    ) {
        return new OutboundBoxSummaryResponse(boxCode, orderCode, productCode, productName, franchiseName, countItem);
    }
}
