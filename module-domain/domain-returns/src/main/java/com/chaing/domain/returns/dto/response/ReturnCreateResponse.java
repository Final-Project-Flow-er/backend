package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.dto.command.FranchiseReturnItemCreateCommand;
import lombok.Builder;

import java.util.List;

@Builder
public record ReturnCreateResponse(
        String returnCode,

        List<FranchiseReturnItemCreateCommand> items
) {
}
