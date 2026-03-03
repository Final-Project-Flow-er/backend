package com.chaing.domain.businessunits.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessUnitType {

    HQ("본사"),
    FRANCHISE("가맹점"),
    FACTORY("공장");

    private final String description;
}
