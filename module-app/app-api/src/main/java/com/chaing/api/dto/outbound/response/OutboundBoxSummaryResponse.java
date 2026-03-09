package com.chaing.api.dto.outbound.response;

public record OutboundBoxSummaryResponse(
        String boxCode,
        String productName,
        String productCode
) {
    public static OutboundBoxSummaryResponse of(
            String boxCode, String productName, String productCode) {
        return new OutboundBoxSummaryResponse(boxCode, productName, productCode);
    }
}
