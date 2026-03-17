package com.chaing.core.enums;

public enum LogType {
    INBOUND,
    OUTBOUND,
    RETURN_OUTBOUND,  // 반품 출고
    RETURN_INBOUND,   // 반품 입고
    SALE,             // 판매
    REFUND,            // 환불
    DISPOSAL,           // 폐기
    SHIPPING,         // 배송중
    SHIPPED,          // 배송 완료

    PICKING,            // 피킹
    PICKING_WAIT,       // 피킹 대기
    INBOUND_WAIT,       // 입고 대기

    AVAILABLE,      // 가용
    RETURN_WAIT,    // 반품 대기
    EXPIRED         // 유통기한 만료
}
