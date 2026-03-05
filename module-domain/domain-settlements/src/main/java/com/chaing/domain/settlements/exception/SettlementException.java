package com.chaing.domain.settlements.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;

import java.util.Objects;

public class SettlementException extends GlobalException {

    public SettlementException(ErrorCode errorCode) {
        super(Objects.requireNonNull(errorCode, "errorCode 값은 필수입니다."));
    }
}
