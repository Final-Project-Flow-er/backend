package com.chaing.domain.returns.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum ReturnType {

    MISORDER("MISORDER", "오발주"),
    PRODUCT_DEFECT("DEFECT", "상품하자");


    private final String key;
    private final String title;
}
