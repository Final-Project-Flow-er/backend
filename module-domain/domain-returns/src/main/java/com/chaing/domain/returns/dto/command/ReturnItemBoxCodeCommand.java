package com.chaing.domain.returns.dto.command;

import com.chaing.domain.returns.entity.ReturnItemBoxCode;
import lombok.Builder;

@Builder
public record ReturnItemBoxCodeCommand(
        Long returnItemBoxCodeId,

        String boxCode,

        Long returnItemId
) {
    public static ReturnItemBoxCodeCommand from(ReturnItemBoxCode returnItemBoxCode) {
        return ReturnItemBoxCodeCommand.builder()
                .returnItemBoxCodeId(returnItemBoxCode.getReturnItemBoxCodeId())
                .boxCode(returnItemBoxCode.getBoxCode())
                .returnItemId(returnItemBoxCode.getReturnItem().getReturnItemId())
                .build();
    }
}
