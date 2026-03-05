package com.chaing.domain.inventories.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InventoriesErrorCode implements ErrorCode {

    INVENTORIES_ALREADY_EXISTS(400, "IV001", "이미 존재하는 제품입니다."),
    INVENTORIES_SERIAL_CODE_IS_NULL(400, "IV002", "제품 식별 코드가 누락되었습니다."),
    INVALID_SERIAL_CODE(400, "IV003", "제품 식별 코드가 유효하지 않습니다."),
    INVENTORIES_MANUFACTURED_DATE_IS_NULL(400, "IV004", "제조일자가 누락되었습니다."),
    INVALID_MANUFACTURED_DATE(400, "IV005", "제조일자가 유효하지 않습니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
