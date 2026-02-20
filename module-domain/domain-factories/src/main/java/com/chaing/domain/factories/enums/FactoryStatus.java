package com.chaing.domain.factories.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FactoryStatus {

    ACTIVE("활성화"),
    INACTIVE("비활성화");

    private final String description;
}
