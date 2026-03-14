package com.chaing.domain.returns.dto.command;

public record FranchiseReturnCommandForTransit(
        Long franchiseOrderId,
        String returnCode,
        Long franchiseId
) {
    public static FranchiseReturnCommandForTransit from(Long franchiseOrderId, String returnCode, Long franchiseId) {
        return new FranchiseReturnCommandForTransit(
                franchiseOrderId,
                returnCode,
                franchiseId
        );
    }
}
