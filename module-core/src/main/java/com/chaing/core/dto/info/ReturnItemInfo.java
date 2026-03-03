package com.chaing.domain.returns.dto.command;

public record ReturnItemCommand(
        String boxCode,

        Long productId
) {
}
