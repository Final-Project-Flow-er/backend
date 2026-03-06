package com.chaing.domain.returns.dto.command;

import com.chaing.domain.returns.entity.ReturnItem;
import lombok.Builder;

import java.util.List;

@Builder
public record ReturnItemCommand(
        Long returnItemId,

        Long returnId,

        Long orderItemId,

        String boxCode
) {
    public static ReturnItemCommand from(ReturnItem returnItem) {
        return ReturnItemCommand.builder()
                .returnItemId(returnItem.getReturnItemId())
                .returnId(returnItem.getReturns().getReturnId())
                .orderItemId(returnItem.getFranchiseOrderItemId())
                .boxCode(returnItem.getBoxCode())
                .build();
    }

    public static List<ReturnItemCommand> from(List<ReturnItem> returnItems) {
        return returnItems.stream()
                .map(ReturnItemCommand::from)
                .toList();
    }
}
