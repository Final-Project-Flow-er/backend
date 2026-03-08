package com.chaing.domain.inventorylogs.dto.response;

import java.time.LocalDate;

public record DailySales(
        LocalDate date,
        Integer quantity
) {
}
