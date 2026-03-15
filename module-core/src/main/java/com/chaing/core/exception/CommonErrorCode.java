package com.chaing.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_PARAMETER(400, "C001", "유효하지 않은 입력값입니다."),
    MISSING_PARAMETER(400, "C002", "필수 입력값이 누락되었습니다."),

    // 403 Forbidden
    IMAGE_DELETE_FORBIDDEN(403, "C003", "해당 이미지에 대한 삭제 권한이 없습니다."),

    // 404 Not Found
    RESOURCE_NOT_FOUND(404, "C004", "자원을 찾을 수 없습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(500, "C999", "서버 내부 오류가 발생했습니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
