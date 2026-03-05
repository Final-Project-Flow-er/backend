package com.chaing.domain.inventories.dto.response;

import java.time.LocalDate;

public record FranchiseInventoryItemResponse(
        String serialCode,
        String boxCode,
        String status,
        LocalDate shippedAt,
        LocalDate receivedAt
) {}