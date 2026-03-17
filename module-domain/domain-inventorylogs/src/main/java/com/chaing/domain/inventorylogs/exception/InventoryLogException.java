package com.chaing.domain.inventorylogs.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;
import lombok.Getter;

@Getter
public class InventoryLogException extends GlobalException {

    private final ErrorCode errorCode;

    public InventoryLogException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
