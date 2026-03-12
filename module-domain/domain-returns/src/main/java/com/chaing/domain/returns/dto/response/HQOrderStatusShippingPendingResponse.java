package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.enums.ReturnStatus;

public record HQOrderStatusShippingPendingResponse(
        String returnCode,

        ReturnStatus status
) {
}
