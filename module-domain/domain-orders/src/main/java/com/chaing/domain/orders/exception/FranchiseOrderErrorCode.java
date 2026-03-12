package com.chaing.domain.orders.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FranchiseOrderErrorCode implements ErrorCode {

    // 404 NOT FOUND
    ORDER_NOT_FOUND(404, "FO001", "해당 발주를 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND(404, "FO002", "해당 발주 제품을 찾을 수 없습니다."),

    PRODUCT_NOT_FOUND(404, "PR001", "해당 제품을 찾을 수 없습니다."),

    // 400 BAD REQUEST
    ORDER_INVALID_STATUS(400, "FO003" ,"발주 상태가 대기일 때만 수정 가능합니다."),
    DATA_OMISSION(400, "RE014", "데이터 누락이 존재합니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
