package com.chaing.domain.orders.dto.reqeust;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record HQOrderUpdateRequest(
        @NotBlank
        LocalDateTime manufactureDate,

        @NotNull
        List<HQOrderItemUpdateRequest> items
) {
}
