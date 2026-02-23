package com.chaing.core.enums;

public enum LogType {
    INBOUND,          // 입고
    OUTBOUND,         // 출고
    RETURN_OUTBOUND,  // 반품출고
    RETURN_INBOUND,   // 반품입고
    SALE,             // 판매
    REFUND,            // 환불

    PICKING,            // 피킹
    PICKING_WAIT,       // 피킹대기
    INBOUND_WAIT,       // 입고대기
    OUTBOUND_PENDING,   // 출고대기

    AVAILABLE,      // 가용
    RETURN_WAIT,    // 반품대기
    EXPIRED         // 유통기한 만료
}
