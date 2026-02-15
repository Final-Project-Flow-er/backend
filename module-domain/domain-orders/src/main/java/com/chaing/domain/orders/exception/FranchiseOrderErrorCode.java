package com.chaing.domain.orders.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FranchiseOrderErrorCode implements ErrorCode {

    // 500 Internal Server Error
    ORDER_NOT_FOUND(400, "FO001", "해당 발주를 찾을 수 없습니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
