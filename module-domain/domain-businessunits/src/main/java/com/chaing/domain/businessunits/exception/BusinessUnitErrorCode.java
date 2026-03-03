package com.chaing.domain.businessunits.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessUnitErrorCode implements ErrorCode {

    BUSINESS_UNIT_NOT_FOUND(404, "B001", "해당 사업장을 찾을 수 없습니다."),
    INVALID_BUSINESS_UNIT_TYPE(400, "B002", "유효하지 않은 사업장 타입입니다."),
    WARNING_ONLY_FOR_FRANCHISE(400, "B003", "가맹점을 위한 경고 로직입니다."),
    CODE_OVERFLOW(400, "B004", "생성 가능한 사업장 코드 범위를 초과했습니다."),
    INVALID_PRODUCTION_LINE_COUNT(400, "B005", "생산 라인 개수는 0 이상이어야 합니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
