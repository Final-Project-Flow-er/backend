package com.chaing.api.dto.hq.response;

import com.chaing.domain.returns.enums.ReturnStatus;
import lombok.Builder;

@Builder
public record HQReturnProductResponse(
        String returnCode,

        ReturnStatus status
) {
}
