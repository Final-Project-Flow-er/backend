package com.chaing.domain.settlements.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PeriodType {
    DAILY("월별"),
    MONTHLY("일별");

    private String description;
}
