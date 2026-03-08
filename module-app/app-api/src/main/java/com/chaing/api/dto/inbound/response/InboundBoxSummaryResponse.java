package com.chaing.api.dto.inbound.response;

public record InboundBoxSummaryResponse(
        String boxCode,
        String productName,
        String productCode
) {
    public static InboundBoxSummaryResponse of(
            String boxCode, String productName, String productCode) {
        return new InboundBoxSummaryResponse(boxCode, productName, productCode);
    }
}
