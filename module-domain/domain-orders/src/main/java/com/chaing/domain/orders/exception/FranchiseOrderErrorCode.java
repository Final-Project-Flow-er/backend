package com.chaing.domain.orders.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FranchiseOrderErrorCode implements ErrorCode {

    // 404 NOT FOUND
    ORDER_NOT_FOUND(404, "FO001", "해당 발주를 찾을 수 없습니다."),

    // 400 BAD REQUEST
    ORDER_INVALID_STATUS(400, "FO002" ,"발주 상태가 대기일 때만 수정 가능합니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
