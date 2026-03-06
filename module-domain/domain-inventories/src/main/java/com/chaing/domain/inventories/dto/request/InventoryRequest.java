package com.chaing.domain.inventories.dto.request;

import com.chaing.core.enums.LogType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InventoryRequest(
        @NotNull
        Long productId,

        @NotBlank
        String serialCode,       // 제품 식별코드

        @NotNull
        LogType productLogType,         // 로그 유형 (가용, 반품, 반품 예정 등)

        @NotNull
        LocalDate manufactureDate // 제조일자

) {
}