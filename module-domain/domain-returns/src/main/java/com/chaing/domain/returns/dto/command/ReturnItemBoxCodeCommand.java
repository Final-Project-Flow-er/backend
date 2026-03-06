package com.chaing.domain.returns.dto.command;

import lombok.Builder;

@Builder
public record ReturnItemBoxCodeCommand(
        Long returnItemBoxCodeId,

        String boxCode,

        Long returnItemId
) {

}
