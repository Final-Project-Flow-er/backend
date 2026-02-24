package com.chaing.api.dto.hq.inventories.request;

import java.time.LocalDate;

public record HQFranchiseItemsRequest(
        Long franchiseId,
        Long productId,
        LocalDate productionDate
) {
}
