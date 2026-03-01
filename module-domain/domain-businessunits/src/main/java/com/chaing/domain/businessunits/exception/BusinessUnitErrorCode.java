package com.chaing.domain.businessunits.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessUnitErrorCode implements ErrorCode {

    BUSINESS_UNIT_NOT_FOUND(404, "B001", "해당 사업장을 찾을 수 없습니다."),
    ;

    private final Integer status;
    private final String code;
    private final String message;
}
