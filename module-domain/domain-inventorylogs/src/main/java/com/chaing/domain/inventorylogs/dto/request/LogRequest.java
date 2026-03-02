package com.chaing.domain.inventorylogs.dto.request;

import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
public record LogRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        String transactionCode
) {
}
