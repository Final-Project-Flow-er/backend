package com.chaing.domain.transports.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransportErrorCode implements ErrorCode {

    TRANSPORT_LOAD_EXCEEDED(400, "TP001", "최대 적재량을 초과하였습니다."),
    TRANSPORT_NOT_FOUND(404, "TP002", "해당 운송 건이 존재하지 않습니다."),
    TRANSPORT_CAN_NOT_CANCEL(400, "TP003", "배송이 진행중이거나 이미 배송이 완료된 건입니다.");

    private final Integer status;
    private final String code;
    private final String message;
}
