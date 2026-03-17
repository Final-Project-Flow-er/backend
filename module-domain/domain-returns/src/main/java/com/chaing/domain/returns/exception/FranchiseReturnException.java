package com.chaing.domain.returns.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;
import lombok.Getter;

@Getter
public class FranchiseReturnException extends GlobalException {

    private final ErrorCode errorCode;

    public FranchiseReturnException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
