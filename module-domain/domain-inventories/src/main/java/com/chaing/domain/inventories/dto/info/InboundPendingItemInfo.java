package com.chaing.domain.inventories.dto.info;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InboundPendingItemInfo(
        @NotBlank String serialCode,
        @NotNull Long productId,
        @NotNull LocalDate manufactureDate,
        @NotNull Long orderId,
        @NotNull Long orderItemId
) {
    public static InboundPendingItemInfo create(
            String serialCode, Long productId, LocalDate manufactureDate, Long orderId, Long orderItemId
    ) {
        return new InboundPendingItemInfo(serialCode, productId, manufactureDate, orderId, orderItemId);
    }
}
