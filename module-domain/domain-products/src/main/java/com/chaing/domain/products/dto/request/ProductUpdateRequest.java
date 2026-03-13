package com.chaing.domain.products.dto.request;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record ProductUpdateRequest(
                String name, // 상품명
                BigDecimal price, // 판매가
                BigDecimal originalPrice, // 원가
                BigDecimal supplyPrice, // 공급가
                String status, // 상태
                Integer kcal, // 칼로리
                LocalDate startDate, // 적용 시작일
                LocalDate endDate, // 적용 종료일
                String description, // 상품 설명
                String imageUrl, // 상품 이미지 URL 또는 경로
                List<String> components // 구성품 이름 리스트
) {
}
