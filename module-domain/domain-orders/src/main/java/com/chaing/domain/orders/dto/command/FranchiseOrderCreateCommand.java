package com.chaing.domain.orders.dto.command;

import com.chaing.domain.orders.dto.info.FranchiseOrderCreateInfo;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FranchiseOrderCreateCommand(
        String username,

        String phoneNumber,

        LocalDateTime deliveryDate,

        String deliveryTime,

        String address,

        String requirement,

        List<FranchiseOrderCreateInfo> items
) {
}
