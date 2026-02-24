package com.chaing.api.dto.hq.inventories.request;

import com.chaing.core.enums.LogType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HQInventoryRequest(
        @NotNull
        Long productId,
        @NotBlank
        String productCode,     // 제품 코드 (예: OR0101)
        @NotNull
        LogType logType,         // 로그 유형 (PRODUCTION, INBOUND, RETURN_IN 등)
        String serialCode,   // 참조 번호 (발주 번호: HEAD2026..., 반품 번호 등)
        @NotNull
        LocalDate manufactureDate // 제조일자

) {
}