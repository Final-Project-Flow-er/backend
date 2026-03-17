package com.chaing.domain.notifications.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    NOTIFICATION_NOT_FOUND(404, "N001", "해당 알림을 찾을 수 없습니다."),
    SSE_SEND_FAIL(500, "N002", "SSE 전송 실패");

    private final Integer status;
    private final String code;
    private final String message;
}
