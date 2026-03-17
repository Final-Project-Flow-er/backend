package com.chaing.domain.returns.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record FranchiseReturnDeliveryResponse(
        String returnCode,

        List<String> boxCodes
) {
    public static FranchiseReturnDeliveryResponse of(String returnCode, List<String> boxCodes) {
        return FranchiseReturnDeliveryResponse.builder()
                .returnCode(returnCode)
                .boxCodes(boxCodes)
                .build();
    }
}
