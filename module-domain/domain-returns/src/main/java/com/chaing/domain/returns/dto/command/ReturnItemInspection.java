package com.chaing.domain.returns.dto.command;

import com.chaing.core.enums.ReturnItemStatus;
import com.chaing.domain.returns.entity.ReturnItem;
import lombok.Builder;

@Builder
public record ReturnItemInspection(
        ReturnItemStatus status,

        String boxCode
) {
    public static ReturnItemInspection from(ReturnItem returnItem) {
        return ReturnItemInspection.builder()
                .status(returnItem.getReturnItemStatus())
                .boxCode(returnItem.getBoxCode())
                .build();
    }
}
