package com.chaing.domain.inventories.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InventoryBatchRequest(
        @NotBlank
        String transactionCode,   // 참조 번호 (발주 번호: HEAD2026..., 반품 번호 등)

        Long orderId,

        Long orderItemId,

        LocalDateTime recordTime,

        @NotNull
        @Positive
        Long fromLocationId,    // 출발지 ID

        @NotBlank
        String fromLocationType,    // 출발지

        @NotNull
        @Positive
        Long toLocationId,    // 도착지 ID

        @NotBlank
        String toLocationType,    // 도착지

        Integer totalQuantity,  // 총 수량

        @NotNull
        @DecimalMin(value = "0", inclusive = true)
        BigDecimal supplyPrice, // 공급가

        LocalDateTime shippedAt, // 배송 완료 시간

        List<InventoryBoxRequest> boxes
) {
}
