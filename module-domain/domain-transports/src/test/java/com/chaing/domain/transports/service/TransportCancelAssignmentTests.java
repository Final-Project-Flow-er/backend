package com.chaing.domain.transports.service;

import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.usecase.executor.TransportExecutor;
import com.chaing.domain.transports.usecase.reader.TransportReader;
import com.chaing.domain.transports.usecase.validator.TransportValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransportCancelAssignmentTests {

    @InjectMocks
    private InternalTransportService transportService;

    @Mock
    private TransportReader reader;
    @Mock private TransportValidator validator;
    @Mock private TransportExecutor executor;

    @Test
    @DisplayName("배차 해제 성공 - PENDING 상태일 때 정상적으로 해제되고 주문코드를 반환한다")
    void cancelAssignment_success() {
        // given
        Long transportId = 1L;
        String expectedOrderCode = "ORD-2026";

        // 리더는 PENDING 상태를 반환한다고 가정
        given(reader.getTransitStatus(transportId)).willReturn(DeliverStatus.PENDING);
        // 익제큐터는 삭제 후 주문코드를 반환한다고 가정
        given(executor.cancelTransit(transportId)).willReturn(expectedOrderCode);

        // when
        String result = transportService.cancelAssignment(transportId);

        // then
        // 1. 밸리데이터가 PENDING 상태로 검증을 수행했는지 확인
        verify(validator).checkCancellable(DeliverStatus.PENDING);
        // 2. 익제큐터가 삭제를 수행했인지 확인
        verify(executor).cancelTransit(transportId);
        // 3. 반환된 주문코드가 맞는지 확인
        assertEquals(expectedOrderCode, result);

        System.out.println("✅ [배차 해제 성공 테스트 완료]");
        System.out.println("   - 해제된 주문 번호: " + result);
    }

    @Test
    @DisplayName("배차 해제 실패 - PENDING이 아닌 상태에서 취소 시도 시 예외가 발생한다")
    void cancelAssignment_fail_invalid_status() {
        // given
        Long transportId = 1L;
        given(reader.getTransitStatus(transportId)).willReturn(DeliverStatus.IN_TRANSIT);

        // 밸리데이터가 예외를 던지도록 설정
        doThrow(new TransportException(TransportErrorCode.TRANSPORT_CAN_NOT_CANCEL))
                .when(validator).checkCancellable(DeliverStatus.IN_TRANSIT);

        // when & then
        TransportException exception = assertThrows(TransportException.class, () ->
                transportService.cancelAssignment(transportId)
        );

        assertEquals(TransportErrorCode.TRANSPORT_CAN_NOT_CANCEL, exception.getErrorCode());

        // 익제큐터(삭제)는 호출 x
        verify(executor, never()).cancelTransit(anyLong());

        System.out.println("✅ [배차 해제 실패 테스트 완료]");
        System.out.println("   - 에러 메시지: " + exception.getMessage());
    }
}
