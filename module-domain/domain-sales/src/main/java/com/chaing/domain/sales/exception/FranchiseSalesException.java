package com.chaing.domain.sales.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;
import lombok.Getter;

@Getter
public class FranchiseSalesException extends GlobalException {

    private final ErrorCode errorCode;

    public FranchiseSalesException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
