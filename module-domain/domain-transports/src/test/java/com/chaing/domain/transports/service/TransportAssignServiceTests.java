//package com.chaing.domain.transports.service;
//
//import com.chaing.domain.transports.dto.OrderInfo;
//import com.chaing.domain.transports.exception.TransportErrorCode;
//import com.chaing.domain.transports.exception.TransportException;
//import com.chaing.domain.transports.usecase.executor.TransportExecutor;
//import com.chaing.domain.transports.usecase.reader.TransportReader;
//import com.chaing.domain.transports.usecase.validator.TransportValidator;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyMap;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(MockitoExtension.class)
//class TransportAssignServiceTests {
//
//    @InjectMocks
//    private InternalTransportService transportService;
//
//    @Mock
//    private TransportReader reader;
//    @Mock
//    private TransportValidator validator;
//    @Mock
//    private TransportExecutor executor;
//
//    @Test
//    @DisplayName("차량 배정 성공 - 모든 검증을 통과하고 익제큐터가 호출된다")
//    void assignVehicle_success() {
//        // given
//        Long vehicleId = 1L;
//        List<OrderInfo> orders = List.of(new OrderInfo(1L, "ORD001", 500L));
//        Map<String, String> trackingMap = Map.of("ORD001", "TRK001");
//        Long totalWeight = 100L;
//
//        given(reader.getVehicleMaxLoad(vehicleId)).willReturn(1000L);
//        given(reader.getCurrentTransitWeight(vehicleId)).willReturn(200L);
//
//        // when
//        transportService.assignVehicle(vehicleId, orders, trackingMap, totalWeight);
//
//        // then
//        // 1. 밸리데이터가 1000(Max), 200(Current), 500(New)으로 호출됐는지 확인
//        verify(validator).checkLoadable(1000L, 200L, 500L);
//        // 2. 익제큐터가 정상적으로 호출됐는지 확인
//        verify(executor).createTransits(eq(vehicleId), anyList(), anyMap());
//
//        System.out.println("✅ 차량 배정 성공 테스트 완료");
//        System.out.println("   - 차량 ID: " + vehicleId);
//        System.out.println("   - 배정 주문 수: " + orders.size());
//    }
//
//    @Test
//    @DisplayName("차량 배정 실패 - 무게 초과 시 예외가 발생한다")
//    void assignVehicle_fail_overload() {
//        // given
//        Long vehicleId = 1L;
//        List<OrderInfo> orders = List.of(new OrderInfo(1L, "ORD001", 900L)); // 무거운 주문
//
//
//        given(reader.getVehicleMaxLoad(vehicleId)).willReturn(1000L);
//        given(reader.getCurrentTransitWeight(vehicleId)).willReturn(200L);
//
//
//        // 밸리데이터가 예외를 던지도록 설정
//        doThrow(new TransportException(TransportErrorCode.TRANSPORT_LOAD_EXCEEDED))
//                .when(validator).checkLoadable(anyLong(), anyLong(), anyLong());
//
//        // when & then
//        TransportException exception = assertThrows(TransportException.class, () ->
//                transportService.assignVehicle(vehicleId, orders, Map.of(), totalWeight)
//        );
//
//        System.out.println("✅ 무게 초과 실패 테스트 완료");
//        System.out.println("   - 발생 에러 코드: " + exception.getErrorCode());
//        System.out.println("   - 에러 메시지: " + exception.getMessage());
//
//        // 익제큐터는 실행되지 않아야 함!
//        verify(executor, never()).createTransits(anyLong(), anyList(), anyMap());
//    }
//}