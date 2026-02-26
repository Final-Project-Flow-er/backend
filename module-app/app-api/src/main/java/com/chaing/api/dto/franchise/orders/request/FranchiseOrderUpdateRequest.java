package com.chaing.api.dto.franchise.orders.request;

import com.chaing.domain.orders.dto.command.FranchiseOrderUpdateCommand;
import com.chaing.domain.orders.dto.info.FranchiseOrderItemInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record FranchiseOrderUpdateRequest(
        @NotBlank
        String username,

        @NotBlank
        String phoneNumber,

        @NotBlank
        String address,

        @NotBlank
        String deliveryTime,

        @Valid
        List<FranchiseOrderItemRequest> items
) {
    public FranchiseOrderUpdateCommand toFranchiseOrderUpdateCommand() {
        return FranchiseOrderUpdateCommand.builder()
                .username(this.username)
                .phoneNumber(this.phoneNumber)
                .address(this.address)
                .deliveryTime(this.deliveryTime)
                .items(FranchiseOrderUpdateRequest.from(this.items))
                .build();
    }

    private static List<FranchiseOrderItemInfo> from(@Valid List<FranchiseOrderItemRequest> items) {
        return items.stream()
                .map(item -> {
                    return new FranchiseOrderItemInfo(
                            item.serialCode(),
                            item.quantity(),
                            item.unitPrice()
                    );
                })
                .toList();
    }
}
