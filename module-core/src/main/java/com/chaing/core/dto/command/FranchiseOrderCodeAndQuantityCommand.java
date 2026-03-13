package com.chaing.core.dto.command;

import com.chaing.core.dto.request.FranchiseOrderCreateRequestItem;
import lombok.Builder;

@Builder
public record FranchiseOrderCodeAndQuantityCommand(
        String productCode,

        Integer quantity
) {
    public static FranchiseOrderCodeAndQuantityCommand from(FranchiseOrderCreateRequestItem franchiseOrderCreateRequestItem) {
        return FranchiseOrderCodeAndQuantityCommand.builder()
                .productCode(franchiseOrderCreateRequestItem.productCode())
                .quantity(franchiseOrderCreateRequestItem.quantity())
                .build();
    }
}
