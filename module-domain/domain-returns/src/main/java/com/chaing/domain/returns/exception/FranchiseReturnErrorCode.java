package com.chaing.domain.returns.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FranchiseReturnErrorCode implements ErrorCode {

    // 404 NOT FOUND
    RETURN_NOT_FOUND(404, "RE001", "해당 반품을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(404, "RE002", "해당 제품을 찾을 수 없습니다."),

    // 409 CONFLICT
    CANCEL_NOT_ALLOWED(409, "RE003", "반품의 상태가 대기일 때만 취소 가능합니다.");

    // 400 BAD REQUEST

    private final Integer status;
    private final String code;
    private final String message;
}
