package com.chaing.api.facade.transport;

import com.chaing.api.dto.transport.internal.request.VehicleAssignmentRequest;
import com.chaing.api.dto.transport.internal.response.AvailableVehicleResponse;
import com.chaing.api.dto.transport.internal.response.TransportCancelResponse;
import com.chaing.domain.transports.service.InternalTransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalTransportFacade {

    private final InternalTransportService transportService;

    // 운송 가능 차량 리스트 조회
    public List<AvailableVehicleResponse> getAvailableVehicle() {
        List<com.chaing.domain.transports.dto.response.AvailableVehicleResponse> domainResponses = transportService.getAvailableVehicle();

        // API용 DTO로 변환
        return domainResponses.stream()
                .map(res -> new AvailableVehicleResponse(
                        res.vehicleId(),
                        res.vehicleNumber(),
                        res.maxLoad(),
                        res.currentWeight(),
                        res.availableWeight()
                ))
                .toList();
    }

    // 차량 배정
    @Transactional
    public void assignVehicle(VehicleAssignmentRequest request) {

        // 발주 도메인
        // 발주 Id, 중량 정보 받아오기
        List<OrderInfo> orders = orderQueryService.getOrderDetails(request.orderIds());


        // 외부 운송 모듈
        // 송장 번호 가져오기
        Map<String, String> trackingMap = externalTrackingModule.getTrackingNumbers(
                orders.stream().map(OrderInfo::getOrderCode).toList()
        );

        // 운송 도메인
        transportService.assignVehicle(
                request.vehicleId(),
                orders, // Long
                trackingMap     // String
        );
        // 정산 관련 도메인
    }

    @Transactional
    public TransportCancelResponse cancelAssignment(Long transportId) {

        // 운송 도메인 해체 로직
        String cancelledOrderCode = transportService.cancelAssignment(transportId);

        // 응답 DTO 생성
        return TransportCancelResponse.from(transportId, cancelledOrderCode);
    }
}
