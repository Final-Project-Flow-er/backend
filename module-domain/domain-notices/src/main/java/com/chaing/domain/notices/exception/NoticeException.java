package com.chaing.domain.notices.exception;

import com.chaing.core.exception.ErrorCode;
import com.chaing.core.exception.GlobalException;
import lombok.Getter;

@Getter
public class NoticeException extends GlobalException {

    private final ErrorCode errorCode;

    public NoticeException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
