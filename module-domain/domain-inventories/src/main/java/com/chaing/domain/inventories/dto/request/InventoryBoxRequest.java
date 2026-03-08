package com.chaing.domain.inventories.dto.request;

import com.chaing.core.enums.LogType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record InventoryBoxRequest(
        @NotBlank
        String boxCode,

        @NotNull
        LogType logType,        // 배송 중, 반품 입고, 반품 출고, 입고, 출고

        @NotBlank
        String productCode,     // 제품 코드 (예: OR0101)

        @NotBlank
        String productName,

        @NotNull
        @DecimalMin(value = "0", inclusive = true)
        BigDecimal price,

        @Valid
        List<InventoryRequest> productList
) {
}
