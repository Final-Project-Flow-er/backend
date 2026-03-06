package com.chaing.domain.notifications.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;
import lombok.Getter;

@Getter
public class NotificationException extends GlobalException {

    private final ErrorCode errorCode;

    public NotificationException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
