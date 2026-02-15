package com.chaing.domain.orders.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;
import lombok.Getter;

@Getter
public class FranchiseOrderException extends GlobalException {

    private final ErrorCode errorCode;

    public FranchiseOrderException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
