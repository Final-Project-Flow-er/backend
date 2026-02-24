package com.chaing.api.dto.hq.inventorylogs.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record HQLogRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        String serialCode
) {}