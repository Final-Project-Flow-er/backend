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
    INVALID_MANUFACTURED_DATE(400, "IV005", "제조일자가 유효하지 않습니다."),
    INVENTORIES_IS_NULL(400, "IV006", "해당 제품이 존재하지 않습니다."),
    INVENTORIES_IS_INVALID(400, "IV007", "해당 상품의 정보가 누락되었습니다."),
    INBOUND_ROLE_INVALID(400, "IV008", "권한이 허용되지 않은 계정입니다."),
    INVALID_INBOUND_STATUS(400, "IV009", "입고 승인이 가능한 상태가 아닙니다."),
    PRODUCT_NOT_FOUND(400, "IV010", "해당 제품이 존재하지 않습니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
