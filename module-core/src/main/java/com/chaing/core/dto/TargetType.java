package com.chaing.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TargetType {

    RETURN("RETURN", "반품"),
    PRODUCT("PRODUCT", "제품");

    private final String key;
    private final String value;
}
