package com.chaing.domain.returns.dto.command;

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
