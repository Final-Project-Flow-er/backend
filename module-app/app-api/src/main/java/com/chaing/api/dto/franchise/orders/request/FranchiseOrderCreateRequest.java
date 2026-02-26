package com.chaing.api.dto.franchise.orders.request;

import com.chaing.domain.orders.dto.command.FranchiseOrderCreateCommand;
import com.chaing.domain.orders.dto.info.FranchiseOrderCreateInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record FranchiseOrderCreateRequest(
        @NotBlank
        String username,

        @NotBlank
        String phoneNumber,

        @NotNull
        LocalDateTime deliveryDate,

        @NotNull
        String deliveryTime,

        @NotBlank
        String address,

        String requirement,

        List<FranchiseOrderCreateRequestItem> items
) {
    public FranchiseOrderCreateCommand toFranchiseOrderCreateCommand() {
        return FranchiseOrderCreateCommand.builder()
                .username(this.username)
                .phoneNumber(this.phoneNumber)
                .deliveryDate(this.deliveryDate)
                .deliveryTime(this.deliveryTime)
                .address(this.address)
                .requirement(this.requirement)
                .items(FranchiseOrderCreateRequest.from(this.items))
                .build();
    }

    private static List<FranchiseOrderCreateInfo> from(List<FranchiseOrderCreateRequestItem> items) {
        return items.stream()
                .map(item -> {
                    return new FranchiseOrderCreateInfo(
                            item.productCode(),
                            item.quantity()
                    );
                })
                .toList();
    }
}
