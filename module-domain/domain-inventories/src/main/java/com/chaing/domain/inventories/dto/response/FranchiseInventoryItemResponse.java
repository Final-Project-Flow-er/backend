package com.chaing.domain.inventories.dto.response;

import java.time.LocalDateTime;

public record FranchiseInventoryItemResponse(
        String serialCode,
        String boxCode,
        String status,
        LocalDateTime shippedAt,
        LocalDateTime receivedAt
) {}