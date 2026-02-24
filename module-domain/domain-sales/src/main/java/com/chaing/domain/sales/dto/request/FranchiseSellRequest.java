package com.chaing.domain.sales.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record FranchiseSellRequest(
        @NotNull
        Integer totalQuantity,

        @NotNull
        @Min(1)
        BigDecimal totalAmount,

        @NotNull
        List<FranchiseSellItemRequest> requestList
) {
}
