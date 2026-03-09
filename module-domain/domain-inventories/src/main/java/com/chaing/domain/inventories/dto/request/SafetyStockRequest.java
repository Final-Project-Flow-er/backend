package com.chaing.domain.inventories.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SafetyStockRequest(
                @NotBlank(message = "위치 타입은 필수입니다.")
                String locationType,

                Long locationId,

                @NotNull(message = "상품 ID는 필수입니다.")
                Long productId,

                @NotNull(message = "안전 재고 수량은 필수입니다.") @PositiveOrZero(message = "안전 재고 수량은 0 이상이어야 합니다.")
                Integer safetyStock) {
}
