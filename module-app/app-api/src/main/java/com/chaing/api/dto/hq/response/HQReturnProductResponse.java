package com.chaing.api.dto.hq.response;

import com.chaing.domain.returns.dto.response.ReturnInfo;
import com.chaing.domain.returns.enums.ReturnStatus;
import lombok.Builder;

@Builder
public record HQReturnProductResponse(
        String returnCode,

        ReturnStatus status
) {
    public static HQReturnProductResponse from(ReturnInfo returnInfo) {
        return HQReturnProductResponse.builder()
                .returnCode(returnInfo.returnCode())
                .status(returnInfo.status())
                .build();
    }
}
