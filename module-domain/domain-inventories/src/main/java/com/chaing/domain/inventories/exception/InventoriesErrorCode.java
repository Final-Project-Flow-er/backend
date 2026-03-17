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
    INVENTORIES_IS_INVALID(400, "IV007", "해당 제품의 정보가 누락되었습니다."),
    INBOUND_ROLE_INVALID(400, "IV008", "권한이 허용되지 않은 계정입니다."),
    INVALID_INBOUND_STATUS(400, "IV009", "입고 승인이 가능한 상태가 아닙니다."),
    PRODUCT_NOT_FOUND(400, "IV010", "해당 제품이 존재하지 않습니다."),
    INVENTORIES_UNMATCHED(400, "IV011", "발주 ID와 식별 코드의 개수가 맞지 않습니다."),
    INVALID_OUTBOUND_STATUS(400, "IV012", "해당 제품은 출고 가능 상태가 아닙니다."),
    INVALID_OUTBOUND_CANCEL_STATUS(400, "IV013", "해당 제품은 출고 취소 가능 상태가 아닙니다."),
    INVENTORIES_BOX_CODE_UNMATCHED(400, "IV014", "해당 제품의 박스 코드가 요청 값과 다릅니다."),
    INVALID_LOCATION_TYPE(400,"IV015","유효하지 않은 제품 타입입니다."),
    INVALID_LOCATION_ID(400,"IV016", "유효하지 않은 ID입니다."),
    DATA_OMISSION(400, "IV017", "데이터 누락이 존재합니다."),
    INVALID_STOCK(400, "IV018", "수량이 부족합니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
