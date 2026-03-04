package com.chaing.domain.notices.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements ErrorCode {

    NOTICE_NOT_FOUND(404, "N001", "해당 공지사항을 찾을 수 없습니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
