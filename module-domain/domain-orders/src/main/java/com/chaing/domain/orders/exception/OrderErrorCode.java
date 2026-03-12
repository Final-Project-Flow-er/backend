package com.chaing.domain.orders.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    // 404 NOT FOUND
    ORDER_NOT_FOUND(404, "OR001", "해당 발주를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(404, "OR002", "해당 제품을 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND(404, "OR003", "해당 발주의 제품을 찾을 수 없습니다."),

    // 400 BAD REQUEST
    INVALID_QUANTITY(404, "OR004", "수량이 올바르지 않습니다."),
    INVALID_STATUS(404, "OR005", "발주의 상태가 올바르지 않습니다."),

    // 403 FORBIDDEN
    UNAUTHORIZED(403, "OR006", "권한이 없습니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
