package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.dto.command.ReturnCommand;
import com.chaing.domain.returns.enums.ReturnStatus;
import lombok.Builder;

@Builder
public record HQOrderStatusShippingPendingResponse(
        String returnCode,

        ReturnStatus status
) {
    public static HQOrderStatusShippingPendingResponse from(ReturnCommand returnCommand) {
        return HQOrderStatusShippingPendingResponse.builder()
                .returnCode(returnCommand.returnCode())
                .status(returnCommand.status())
                .build();
    }
}
