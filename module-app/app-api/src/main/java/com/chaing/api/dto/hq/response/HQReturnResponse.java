package com.chaing.api.dto.hq.response;

import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record HQReturnResponse(
        @NotBlank
        String franchiseCode,

        @NotNull
        LocalDateTime requestedDate,

        @NotBlank
        String returnCode,

        @NotNull
        ReturnStatus status,

        @NotBlank
        String productCode,

        @NotNull
        ReturnType type,

        @NotNull
        @Min(1)
        Integer quantity,

        @NotNull
        BigDecimal totalPrice,

        @NotBlank
        String receiver,

        @NotBlank
        String phoneNumber,

        @NotBlank
        String boxCode
) {
}
