package com.chaing.domain.sales.dto.response;

import java.time.LocalDate;

public record FranchiseSalesDailyQuantityResponse(
        Long franchiseId,
        Long productId,
        LocalDate date,
        Integer quantity
) {
}
