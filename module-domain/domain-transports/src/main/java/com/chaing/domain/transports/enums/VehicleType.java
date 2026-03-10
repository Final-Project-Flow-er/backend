package com.chaing.domain.transports.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleType {

    CARGO("카고"),
    WING_BODY("윙바디"),
    REFRIGERATED_TOP("냉동탑차"),
    CHILLED_TOP("냉장탑차"),
    CONTAINER("컨테이너 캐리어");

    private final String description;
}
