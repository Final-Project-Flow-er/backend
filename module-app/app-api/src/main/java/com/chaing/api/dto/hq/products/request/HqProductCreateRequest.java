package com.chaing.api.dto.hq.products.request;

import com.chaing.domain.products.enums.ProductStatus;
import com.chaing.domain.products.enums.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record HqProductCreateRequest(
        // 제품 코드 (OR, RO, MA 등 구분 가능)
        @NotBlank
        String productCode,

        // 제품명
        @NotBlank
        String name,

        // 설명
        @NotBlank
        String description,

        // OR, RO, MA 등 제품 타입
        @NotNull
        ProductType type,

        // 이미지 URL
        @NotBlank
        String imageUrl,

        // 소비자 판매가
        @NotNull
        BigDecimal price,

        // 원가
        @NotNull
        BigDecimal costPrice,

        // 공급가
        @NotNull
        BigDecimal supplyPrice,

        // 안전재고
        @NotNull
        Integer safetyStock,

        // 판매 상태
        @NotNull
        ProductStatus status,

        // 칼로리
        @NotNull
        Integer kcal,

        // 무게(g)
        @NotNull
        Integer weight
) {
}
