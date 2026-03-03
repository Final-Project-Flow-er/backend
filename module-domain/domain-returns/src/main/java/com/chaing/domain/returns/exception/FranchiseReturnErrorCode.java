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
    ORDER_ITEM_NOT_FOUND(404, "RE003", "해당 제품을 찾을 수 없습니다."),
    RETURN_ITEM_NOT_FOUND(404, "RE004", "해당 반품 요청에 속한 제품을 찾을 수 없습니다."),
    INVENTORY_NOT_FOUND(404, "RE005", "해당 반품 요청에 대한 재고가 존재하지 않습니다."),

    // 409 CONFLICT
    CANCEL_NOT_ALLOWED(409, "RE004", "반품의 상태가 대기일 때만 취소 가능합니다."),

    // 400 BAD REQUEST
    INVALID_PRODUCT_INFO(400, "RE005", "제품에 대한 정보가 옳지 않습니다."),
    INVALID_BOX_CODE(400, "RE006", "재고에 존재하지 않는 박스 코드입니다."),

    // 401 FORBIDDEN
    USER_FORBIDDEN(401, "RE007", "사용자 권한이 없습니다.");

    // 400 BAD REQUEST

    private final Integer status;
    private final String code;
    private final String message;
}
