package com.chaing.api.dto.hq.products.request;

import lombok.Builder;

@Builder
public record HQProductSearchRequest(
        String productCode,  // 상품 코드 (예: OR0101)
        String name,         // 상품 명
        String status,       // 상태
        String sizeCode      // 인분 (01: 1~2인분, 03: 3~4인분)
) {}