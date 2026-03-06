package com.chaing.api.dto.transport.internal.response;

public record TransportCancelResponse(
        Long transportId,
        String orderCode,
        String message
) {
    public static TransportCancelResponse from(Long id, String orderCode) {
        return new TransportCancelResponse(id, orderCode, "배차가 성공적으로 해제되었습니다.");
    }
}
