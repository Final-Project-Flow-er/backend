package com.chaing.domain.transports.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;
import lombok.Getter;

@Getter
public class TransportException extends GlobalException {

    private final ErrorCode errorCode;

    public TransportException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }}
