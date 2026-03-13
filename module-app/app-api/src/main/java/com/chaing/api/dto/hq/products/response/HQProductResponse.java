package com.chaing.api.dto.hq.products.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record HQProductResponse(
        Long productId, // 제품 고유 번호
        String name, // 제품이름
        String productCode, // 제품코드
        String description, // 제품 설명
        String imageUrl, // 제품 이미지 URL
        String size, // 제품 사이즈
        String spicy, // 맵기
        Integer kcal, // 제품 칼로리
        Integer weight, // 무게
        BigDecimal price, // 판매가
        BigDecimal supplyPrice, // 공급가
        BigDecimal costPrice, // 원가
        LocalDate startDate,
        LocalDate endDate,
        List<String> components, // 구성품
        String status // 상태
) {
}
