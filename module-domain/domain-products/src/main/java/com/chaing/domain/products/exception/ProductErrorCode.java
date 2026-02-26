package com.chaing.domain.products.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    // 404 NOT FOUND
    PRODUCT_NOT_FOUND(404, "PR001", "해당 상품을 찾을 수 없습니다."),
    PRODUCT_TYPE_NOT_FOUND(404, "PR002", "존재하지 않는 상품 타입 코드입니다."),

    // 400 BAD REQUEST
    INVALID_PRODUCT_CODE_FORMAT(400, "PR003", "상품 코드 형식이 올바르지 않습니다."),

    // 409 CONFLICT
    DUPLICATE_PRODUCT_CODE(409, "PR004", "상품 코드가 중복됩니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
