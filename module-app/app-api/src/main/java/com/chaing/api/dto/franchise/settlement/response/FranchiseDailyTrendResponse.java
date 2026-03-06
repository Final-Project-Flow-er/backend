package com.chaing.api.dto.franchise.settlement.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FranchiseDailyTrendResponse(
        LocalDate date,                // x축: 날짜
        BigDecimal amount              // y축: 매출액
) {
}
