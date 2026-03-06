package com.chaing.domain.returns.dto.command;

import lombok.Builder;

@Builder
public record FranchiseReturnItemCreateCommand(
        String boxCode,

        String productCode
) {
}
