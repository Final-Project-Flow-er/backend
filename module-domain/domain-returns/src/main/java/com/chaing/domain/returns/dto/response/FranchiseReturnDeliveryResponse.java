package com.chaing.domain.returns.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record FranchiseReturnDeliveryResponse(
        String returnCode,

        List<String> boxCodes
) {
}
