package com.chaing.domain.inventorylogs.dto.request;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.enums.LocationType;

public record InventoryLogCreateRequest(
        Long productId,
        String productName,
        String boxCode,
        String transactionCode,
        LogType logType,
        Integer quantity,
        LocationType fromLocationType,
        Long fromLocationId,
        LocationType toLocationType,
        Long toLocationId,
        ActorType actorType,
        Long actorId) {
}