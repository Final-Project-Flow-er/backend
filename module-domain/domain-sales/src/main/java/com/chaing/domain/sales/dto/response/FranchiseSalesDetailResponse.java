package com.chaing.domain.sales.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FranchiseSalesDetailResponse(
        @NotBlank
        String salesCode,

        @NotNull
        LocalDateTime salesDate,

        @NotNull
        List<FranchiseSalesProductResponse> products
) {
}
