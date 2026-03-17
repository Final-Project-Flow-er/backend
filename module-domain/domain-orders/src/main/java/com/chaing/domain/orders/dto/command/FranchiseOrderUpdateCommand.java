package com.chaing.domain.orders.dto.command;

import com.chaing.domain.orders.dto.info.FranchiseOrderItemInfo;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FranchiseOrderUpdateCommand(
        String username,
        String phoneNumber,
        String address,
        String deliveryTime,
        String requirement,
        List<FranchiseOrderItemInfo> items
) {

}
