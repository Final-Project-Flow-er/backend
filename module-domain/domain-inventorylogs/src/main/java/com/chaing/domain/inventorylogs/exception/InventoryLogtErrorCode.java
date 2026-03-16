package com.chaing.domain.inventorylogs.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InventoryLogtErrorCode implements ErrorCode {

    // 400 BAD REQUEST
    INVALID_ACTOR_TYPE(400, "IL001", "잘못된 행위자 타입 값입니다."),
    INVALID_LOG_TYPE(400, "IL002", "잘못된 로그 타입 값입니다."),
    INVALID_INPUT(400, "IL003", "잘못된 입력 값입니다."),
    PAGE_WINDOW_TOO_LARGE(400, "IL004", "조회 범위가 너무 큽니다. 조회 기간을 좁혀주세요.");

    private final Integer status;
    private final String code;
    private final String message;
}
