package com.chaing.domain.settlements.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;
import lombok.Getter;

@Getter
public class SettlementException extends GlobalException {

    private final ErrorCode errorCode;

    public SettlementException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;;

    }
}
