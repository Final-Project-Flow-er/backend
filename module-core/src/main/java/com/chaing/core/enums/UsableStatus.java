package com.chaing.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UsableStatus {

    ACTIVE("활성화"),
    INACTIVE("비활성화");

    private final String description;
}
