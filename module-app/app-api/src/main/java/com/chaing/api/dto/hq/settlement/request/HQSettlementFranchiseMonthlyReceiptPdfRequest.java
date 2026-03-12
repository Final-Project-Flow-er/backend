package com.chaing.api.dto.hq.settlement.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

public record HQSettlementFranchiseMonthlyReceiptPdfRequest(
        @NotNull Long franchiseId,
        @NotNull @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
}
