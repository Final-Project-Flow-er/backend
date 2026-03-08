package com.chaing.domain.inventories.dto.response;

import java.time.LocalDate;

public record ExpirationAlertResponse(
        String productName,             // 제품 이름
        LocalDate manufactureDate,      // 제조일자
        Integer quantity,                  // 현재 수량
        Integer daysUntilExpiration        // 제조 일자

) {}