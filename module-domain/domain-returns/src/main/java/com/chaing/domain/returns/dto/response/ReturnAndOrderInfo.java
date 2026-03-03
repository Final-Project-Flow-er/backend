package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.entity.ReturnItem;

import java.util.List;

public record ReturnItemInfo(
        Long orderItemId,

        Long returnItemId
) {
    public static ReturnItemInfo from(ReturnItem returnItem) {
        return new ReturnItemInfo(
                returnItem.getFranchiseOrderItemId(),
                returnItem.getReturnItemId()
        );
    }

    public static List<ReturnItemInfo> from(List<ReturnItem> returnItems) {
        return returnItems.stream()
                .map(ReturnItemInfo::from)
                .toList();
    }
}
