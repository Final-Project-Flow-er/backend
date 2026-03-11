package com.chaing.domain.returns.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FranchiseReturnDeliveryResponse(
        String returnCode,

        List<String> boxCodes
) {
}
