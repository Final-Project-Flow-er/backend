package com.chaing.domain.inventories.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {

    // 404 NOT FOUND
    LOCATION_NOT_FOUND(404, "LOC001", "해당 위치를 찾을 수 없습니다."),

    // 400 BAD REQUEST
    INVALID_LOCATION_TYPE(400, "LOC002", "유효하지 않은 LocationType입니다."),
    INVALID_LOCATION_ID(400, "LOC003", "유효하지 않은 LocationId입니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
