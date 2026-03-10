package com.chaing.domain.orders.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HQOrderErrorCode implements ErrorCode {

    // 404 NOT FOUND
    ORDER_NOT_FOUND(404, "HO001", "해당 발주를 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND(404, "HO002", "해당 발주의 제품을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(404, "HO003", "해당 제품을 찾을 수 없습니다."),

    // 400 BAD REQUEST
    INVALID_INPUT(400, "HO004", "잘못된 입력값입니다."),
    INVALID_STATUS(400, "HO005", "발주의 상태가 올바르지 않습니다."),
    INVALID_USER_INFO(400, "HO006", "사용자 정보가 올바르지 않습니다."),
    DATA_OMISSION(400, "HO007", "데이터가 누락되었습니다."),

    // 409 CONFLICT
    ORDER_ALREADY_CANCELED(409, "HO008", "이미 취소된 발주입니다."),
    ORDER_NOT_PENDING(409, "HO009", "상태가 대기인 발주만 취소 가능합니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
