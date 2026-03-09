package com.chaing.domain.inventories.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;
import lombok.Getter;

@Getter
public class InventoriesException extends GlobalException {

    private final ErrorCode errorCode;

    public InventoriesException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }}
