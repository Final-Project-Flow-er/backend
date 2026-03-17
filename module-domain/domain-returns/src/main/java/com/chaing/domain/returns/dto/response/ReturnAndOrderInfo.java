package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.entity.ReturnItem;

import java.util.List;

public record ReturnAndOrderInfo(
        Long orderItemId,

        Long returnItemId
) {
    public static ReturnAndOrderInfo from(ReturnItem returnItem) {
        return new ReturnAndOrderInfo(
                returnItem.getFranchiseOrderItemId(),
                returnItem.getReturnItemId()
        );
    }

    public static List<ReturnAndOrderInfo> from(List<ReturnItem> returnItems) {
        return returnItems.stream()
                .map(ReturnAndOrderInfo::from)
                .toList();
    }
}
