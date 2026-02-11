package com.chaing.api.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;

    // ErrorCode를 받아서 객체를 만드는 정적 메서드 (편의성)
    public static ErrorResponse from(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatus()) // 여기서 status 꺼냄!
                .code(errorCode.getCode())     // 여기서 code 꺼냄!
                .message(errorCode.getMessage())
                .build();
    }
}
