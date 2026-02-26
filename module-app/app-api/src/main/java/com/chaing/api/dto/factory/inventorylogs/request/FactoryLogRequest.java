package com.chaing.api.dto.factory.inventorylogs.request;

import com.chaing.core.enums.LogType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record FactoryLogRequest(
        String productName,
        LogType logType,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        String serialCode
) {
}
