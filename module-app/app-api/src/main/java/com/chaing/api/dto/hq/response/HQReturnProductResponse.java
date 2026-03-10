package com.chaing.api.dto.hq.response;

import com.chaing.domain.returns.dto.command.ReturnCommand;
import com.chaing.domain.returns.dto.response.ReturnInfo;
import com.chaing.domain.returns.enums.ReturnStatus;
import lombok.Builder;

@Builder
public record HQReturnProductResponse(
        String returnCode,

        ReturnStatus status
) {
    public static HQReturnProductResponse from(ReturnCommand returnCommand) {
        return HQReturnProductResponse.builder()
                .returnCode(returnCommand.returnCode())
                .status(returnCommand.status())
                .build();
    }
}
