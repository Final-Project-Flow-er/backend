package com.chaing.api.dto.franchise.products.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record FranchiseProductResponse(
        String name, // 제품이름
        String productCode, // 제품코드
        String description, // 제품 설명
        String size, // 제품 사이즈
        String spicy, // 맵기
        Integer kcal, // 제품 칼로리
        Integer weight, // 무게
        Integer safetyStock, // 안전 재고
        BigDecimal price, // 판매가
        BigDecimal supplyPrice, // 공급가
        LocalDate startDate,
        LocalDate endDate,
        List<String> components, // 구성품
        String status // 상태
) {
}
