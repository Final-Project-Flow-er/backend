package com.chaing.domain.inventories.dto.response;

import java.time.LocalDate;

public record ExpirationBatchResultResponse(
        Long productId,
        LocalDate manufactureDate,
        Integer quantity,
        Integer daysUntilExpiration
) {
}
