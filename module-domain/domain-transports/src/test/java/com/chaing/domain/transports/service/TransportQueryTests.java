package com.chaing.domain.transports.service;

import com.chaing.domain.transports.dto.response.AvailableVehicleResponse;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.usecase.reader.TransportReader;
import com.chaing.domain.transports.usecase.validator.TransportValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TransportQueryTests {

    @InjectMocks
    private InternalTransportService transportService;

    @Mock
    private TransportReader reader;
    @Mock
    private TransportValidator validator;

    @Test
    @DisplayName("차량 목록 조회 시, validator의 검증을 통과한 차량만 반환된다")
    void getAvailableVehicle_filtering_test() {
        // given
        // 1. 후보 차량 2대 생성
        Vehicle v1 = Vehicle.builder().vehicleId(1L).vehicleNumber("차량1").maxLoad(1000L).build();
        Vehicle v2 = Vehicle.builder().vehicleId(2L).vehicleNumber("차량2").maxLoad(2000L).build();

        given(reader.findCandidateVehicles()).willReturn(List.of(v1, v2));

        // 2. 각 차량의 현재 적재량 설정
        given(reader.getCurrentTransitWeight(1L)).willReturn(500L);
        given(reader.getCurrentTransitWeight(2L)).willReturn(1800L);

        // 3. Validator 조건 설정 (1번 차량은 통과, 2번 차량은 탈락)
        given(validator.canLoadWeight(1000L, 500L)).willReturn(true);
        given(validator.canLoadWeight(2000L, 1800L)).willReturn(false);

        // when
        List<AvailableVehicleResponse> result = transportService.getAvailableVehicle();

        // then
        // 결과에는 v1만 있어야 함!
        assertEquals(1, result.size());
        assertEquals("차량1", result.get(0).vehicleNumber());

        // 1번 차량의 경우 filter와 map에서 호출되므로 2번
        verify(reader, times(2)).getCurrentTransitWeight(1L);
        // 2번 차량의 경우 filter에서 걸러지므로 1번
        verify(reader, times(1)).getCurrentTransitWeight(2L);

        System.out.println("========================================");
        System.out.println("✅ [조회 필터링 테스트 완료]");
        System.out.println("   - 전체 후보: 2대");
        System.out.println("   - 필터 통과: " + result.get(0).vehicleNumber() + " (1대)");
        System.out.println("========================================");
    }
}
