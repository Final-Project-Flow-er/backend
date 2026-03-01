package com.chaing.domain.transports.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransportErrorCode implements ErrorCode {

    TRANSPORT_LOAD_EXCEEDED(400, "TP001", "최대 적재량을 초과하였습니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
