package com.chaing.core.dto.command;

import com.chaing.core.dto.request.FranchiseOrderCreateRequestItem;
import com.chaing.core.dto.request.FranchiseOrderUpdateRequest;
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

    public static FranchiseOrderCodeAndQuantityCommand from(FranchiseOrderUpdateRequest franchiseOrderCreateRequestItem) {
        return FranchiseOrderCodeAndQuantityCommand.builder()
                .productCode(franchiseOrderCreateRequestItem.productCode())
                .quantity(franchiseOrderCreateRequestItem.quantity())
                .build();
    }
}
