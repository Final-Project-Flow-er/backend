package com.chaing.domain.inventories.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;
import lombok.Getter;

@Getter
public class InventoryException extends GlobalException {

    private final ErrorCode errorCode;

    public InventoryException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
