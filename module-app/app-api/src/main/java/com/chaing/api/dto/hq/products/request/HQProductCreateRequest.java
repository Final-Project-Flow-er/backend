package com.chaing.api.dto.hq.products.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record HQProductCreateRequest(

                // 제품 코드 (OR, RO, MA 등 구분 가능)
                @NotBlank String productCode,

                // 제품명
                @NotBlank String name,

                // 설명
                @NotBlank String description,

                // 이미지 URL(직접 입력 시). 파일 업로드 시 null 가능
                String imageUrl,

                // 소비자 판매가
                @NotNull BigDecimal price,

                // 원가
                @NotNull BigDecimal costPrice,

                // 공급가
                @NotNull BigDecimal supplyPrice,

                // 안전재고
                @NotNull Integer safetyStock,

                // 판매 상태
                @NotNull String status,

                // 칼로리
                @NotNull Integer kcal,

                // 무게(g)
                @NotNull Integer weight,

                // 공급가 적용일
                LocalDate startDate,

                LocalDate endDate,

                // 구성품 목록 (이름 리스트)
                List<String> components) {
}
