package com.chaing.api.dto.inbound.request;

import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
import com.chaing.domain.inventories.dto.info.InboundPendingItemInfo;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record InboundScanBoxRequest(
        @NotBlank String boxCode
) {
    public static FranchiseInboundCreateCommand toCommand(InboundScanBoxRequest request, List<InboundPendingItemInfo> itemInfos, Long franchiseId) {
        if(itemInfos == null || itemInfos.isEmpty()) {
            throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_NULL);
        }
        InboundPendingItemInfo firstItem = itemInfos.get(0);

        List<String> serialCodes = itemInfos.stream().map(InboundPendingItemInfo::serialCode).toList();
        List<Long> orderItemIds =  itemInfos.stream().map(InboundPendingItemInfo::orderItemId).toList();

        return new FranchiseInboundCreateCommand(
                request.boxCode(),
                serialCodes,
                firstItem.productId(),
                firstItem.manufactureDate(),
                franchiseId,
                firstItem.orderId(),
                orderItemIds
        );
    }
}
