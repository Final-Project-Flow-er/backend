package com.chaing.domain.inventories.dto.request;

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
