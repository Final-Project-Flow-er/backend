package com.chaing.domain.transports.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Dispatchable {
    AVAILABLE("배차 가능"),
    UNAVAILABLE("배차 불가"),
    DISPATCHED("배차 완료");

    private final String description;
}
