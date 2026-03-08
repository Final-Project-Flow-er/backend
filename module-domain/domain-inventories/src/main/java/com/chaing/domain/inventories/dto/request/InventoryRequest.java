package com.chaing.domain.inventories.dto.request;

import com.chaing.core.enums.LogType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record InventoryRequest(
        @NotNull
        @Positive
        Long productId,

        @NotBlank
        String serialCode,       // 제품 식별코드

        Long orderItemId,

        @NotNull
        LogType productLogType,         // 로그 유형 (가용, 반품, 반품 예정 등)

        @NotNull
        @PastOrPresent
        LocalDate manufactureDate // 제조일자

) {
}