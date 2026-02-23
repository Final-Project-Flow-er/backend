package com.chaing.domain.franchises.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FranchiseStatus {

    ACTIVE("활성화"),
    INACTIVE("비활성화");

    private final String description;
}
