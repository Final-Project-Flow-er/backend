package com.chaing.domain.inventories.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record HQInventoryBatchResponse(
        LocalDate manufactureDate,
        Integer totalQuantity
) {
}
