package com.chaing.domain.returns.dto.command;

import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.enums.ReturnItemStatus;
import lombok.Builder;

@Builder
public record ReturnItemInspection(
        Boolean isInspected,

        ReturnItemStatus status
) {
    public static ReturnItemInspection from(ReturnItem returnItem) {
        return ReturnItemInspection.builder()
                .isInspected(returnItem.getIsInspected())
                .status(returnItem.getReturnItemStatus())
                .build();
    }
}
