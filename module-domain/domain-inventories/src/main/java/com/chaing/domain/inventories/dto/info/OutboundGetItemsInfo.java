package com.chaing.domain.inventories.dto.info;

import java.time.LocalDate;

public record OutboundGetItemsInfo(
        Long productId,
        String serialCode,
        LocalDate manufactureDate
) {
}
