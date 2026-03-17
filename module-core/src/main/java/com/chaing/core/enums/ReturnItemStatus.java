package com.chaing.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReturnItemStatus {

    BEFORE_INSPECTION("BEFORE", "검수 전"),
    NORMAL("NORMAL", "정상"),
    DEFECTIVE("DEFECTIVE", "하자");

    private final String key;
    private final String value;
}
