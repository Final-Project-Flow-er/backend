package com.chaing.domain.inventorylogs.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record FranchiseLogRequest(
        String productName,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        String transactionCode
) {
}
