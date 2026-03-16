package com.chaing.domain.users.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    // 400 BAD REQUEST
    INVALID_POSITION_FOR_ROLE(400, "U001", "권한과 일치하지 않는 역할입니다."),
    INVALID_PASSWORD_FORMAT(400, "U002", "올바르지 않은 비밀번호 형식입니다."),
    INVALID_PASSWORD(400, "U003", "올바르지 않은 비밀번호입니다."),
    EMAIL_MISMATCH(400, "U004", "아이디와 이메일이 일치하지 않습니다."),
    INVALID_ROLE(400, "U005", "유효하지 않은 권한입니다."),
    EMAIL_NOT_FOUND(400, "U009", "이메일을 찾을 수 없습니다."),

    // 401 UNAUTHORIZED
    REFRESH_TOKEN_NOT_FOUND(401, "A001", "세션이 만료되었습니다. 다시 로그인해 주세요."),
    INVALID_TOKEN(401, "A002", "유효하지 않은 토큰입니다."),
    TOKEN_MISMATCH(401, "A003", "토큰 정보가 일치하지 않습니다."),
    INVALID_LOGIN_CREDENTIALS(401, "A008", "아이디 또는 비밀번호가 일치하지 않습니다."),

    // 403 FORBIDDEN
    ACCESS_DENIED(403, "A004", "접근 권한이 없습니다."),
    INACTIVATED_USER(403, "A005", "비활성화된 계정입니다."),
    DELETED_USER(403, "A006", "삭제된 계정입니다."),
    INVALID_BUSINESS_UNIT_ACCESS(403, "A007", "연결된 사업장이 없거나 접근 권한이 없습니다."),

    // 404 NOT FOUND
    USER_NOT_FOUND(404, "U006", "해당 회원을 찾을 수 없습니다."),
    BUSINESS_UNIT_NOT_FOUND(404, "U007", "사업장이 존재하지 않습니다."),

    // 500 INTERNAL SERVER ERROR
    HASH_ALGORITHM_NOT_FOUND(500, "S001", "해싱 알고리즘을 찾을 수 없습니다."),
    MAIL_SEND_FAILED(500, "S002", "메일 발송을 실패했습니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
