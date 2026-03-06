package com.chaing.api.dto.hq.settlement.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record HQSettlementFranchiseDailyReceiptPdfRequest(
        @NotNull Long franchiseId,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date
) {
}
