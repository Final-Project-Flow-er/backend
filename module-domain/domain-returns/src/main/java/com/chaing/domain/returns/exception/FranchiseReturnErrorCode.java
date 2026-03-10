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
    RETURN_ITEM_BOX_CODE_NOT_FOUND(404, "RE006", "해당 반품 제품에 대한 재고가 존재하지 않습니다."),

    // 409 CONFLICT
    CANCEL_NOT_ALLOWED(409, "RE007", "반품의 상태가 대기일 때만 취소 가능합니다."),
    ALREADY_ACCEPTED(409, "RE008", "이미 접수된 상태입니다."),

    // 400 BAD REQUEST
    INVALID_PRODUCT_INFO(400, "RE009", "제품에 대한 정보가 옳지 않습니다."),
    INVALID_BOX_CODE(400, "RE010", "재고에 존재하지 않는 박스 코드입니다."),
    INVALID_REQUEST(400, "RE011", "올바르지 않은 요청입니다."),
    INVALID_RETURN_ITEM_STATUS(400, "RE012", "반품 제품이 검수 전이 아닙니다."),
    INVALID_RETURN_STATUS(400, "RE013", "반품의 상태가 배송완료가 아닙니다."),
    DATA_OMISSION(400, "RE014", "데이터 누락이 존재합니다."),

    // 403 FORBIDDEN
    USER_FORBIDDEN(403, "RE015", "사용자 권한이 없습니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
