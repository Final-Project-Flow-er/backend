package com.chaing.domain.inventories.dto.request;

import java.util.List;

public record DisposalRequest(
        String actorType,
        Long actorId,
        List<Long> inventoryIds
) {
}
