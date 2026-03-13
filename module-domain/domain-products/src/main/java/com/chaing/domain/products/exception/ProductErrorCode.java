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
    COMPONENT_NOT_FOUND(404, "PR006", "해당 구성용품을 찾을 수 없습니다."),

    // 400 BAD REQUEST
    INVALID_PRODUCT_CODE_FORMAT(400, "PR003", "상품 코드 형식이 올바르지 않습니다."),
    INVALID_PRODUCT_STATUS(400, "PR005", "잘못된 상품 상태 값입니다."),
    INVALID_COMPONENT_NAME(400, "PR007", "구성용품 이름이 올바르지 않습니다."),

    // 409 CONFLICT
    DUPLICATE_PRODUCT_CODE(409, "PR004", "상품 코드가 중복됩니다."),
    COMPONENT_IN_USE(409, "PR008", "상품에 사용 중인 구성용품은 삭제할 수 없습니다."),
    DUPLICATE_COMPONENT_NAME(409, "PR009", "구성용품 이름이 중복됩니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
