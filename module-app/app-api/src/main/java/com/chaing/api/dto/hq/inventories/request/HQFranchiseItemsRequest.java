package com.chaing.api.dto.hq.inventories.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HQFranchiseItemsRequest(
        @NotNull
        Long franchiseId,
        @NotNull
        Long productId,
        LocalDate manufactureDate
) {
}
