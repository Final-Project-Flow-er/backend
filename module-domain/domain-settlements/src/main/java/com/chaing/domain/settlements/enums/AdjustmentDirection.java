package com.chaing.domain.settlements.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdjustmentDirection {
    INCREASE("증가"),
    DECREASE("차감");

    private final String description;
}
