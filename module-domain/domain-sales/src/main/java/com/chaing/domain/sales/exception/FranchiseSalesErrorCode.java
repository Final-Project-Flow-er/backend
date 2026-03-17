package com.chaing.domain.sales.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FranchiseSalesErrorCode implements ErrorCode {

    // 404 NOT FOUND
    SALES_NOT_FOUND(404, "SA001", "해당 판매 기록을 찾을 수 없습니다."),

    // 409 CONFLICT
    DUPLICATE_SALES_CODE(409, "SA002", "판매 코드가 중복됩니다."),
    DUPLICATE_LOT(409, "SA003", "제품 식별 코드가 중복됩니다."),

    // 400 BAD REQUEST
    ALREADY_CANCELLED(400, "SA004", "이미 취소된 주문입니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
