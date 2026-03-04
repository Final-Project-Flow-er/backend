package com.chaing.domain.orders.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    // 404 NOT FOUND
    ORDER_NOT_FOUND(404, "FO001", "해당 발주를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(404, "HO003", "해당 제품을 찾을 수 없습니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
