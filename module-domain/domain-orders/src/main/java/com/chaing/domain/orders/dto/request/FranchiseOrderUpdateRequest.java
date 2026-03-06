package com.chaing.domain.orders.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FranchiseOrderUpdateRequest(
        @NotBlank
        String productCode,

        @NotNull
        Integer quantity
) {

}
