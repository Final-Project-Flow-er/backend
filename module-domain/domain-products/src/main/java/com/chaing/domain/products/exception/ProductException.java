package com.chaing.domain.products.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;
import lombok.Getter;

@Getter
public class ProductException extends GlobalException {

    private final ErrorCode errorCode;

    public ProductException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
