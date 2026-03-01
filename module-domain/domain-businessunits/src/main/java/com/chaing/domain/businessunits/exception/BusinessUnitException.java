package com.chaing.domain.businessunits.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;

public class BusinessUnitException extends GlobalException {

    private final ErrorCode errorCode;

    public BusinessUnitException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
