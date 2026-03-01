package com.chaing.domain.transports.service;

import com.chaing.domain.transports.dto.OrderInfo;
import com.chaing.domain.transports.dto.request.TransportForceUpdateRequest;
import com.chaing.domain.transports.dto.request.VehicleAssignmentRequest;
import com.chaing.domain.transports.dto.response.AvailableVehicleResponse;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.usecase.executor.TransportExecutor;
import com.chaing.domain.transports.usecase.reader.TransportReader;
import com.chaing.domain.transports.usecase.validator.TransportValidator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalTransportService {

    private final TransportExecutor executor;
    private final TransportReader reader;
    private final TransportValidator validator;

    // 운송 가능 차량 목록 조회
    public List<AvailableVehicleResponse> getAvailableVehicle() {

        // 활성화 + 배차 가능 상태의 차량 목록 조회
        List<Vehicle> candidateVehicles = reader.findCandidateVehicles();

        return candidateVehicles.stream()
                .filter(vehicle -> {
                    // 1. 현재 적재량
                    Long currentTransitWeight = reader.getCurrentTransitWeight(vehicle.getVehicleId());

                    // 2. 적재 가능 여부 검증
                    return validator.canLoadWeight(vehicle.getMaxLoad(), currentTransitWeight);
                })
                .map(vehicle -> {
                    Long currentWeight = reader.getCurrentTransitWeight(vehicle.getVehicleId());
                    return AvailableVehicleResponse.from(vehicle, currentWeight);
                })
                .toList();
    }

    // 차량 배정
    public void assignVehicle(
            @NotNull(message = "차량을 선택해주세요") Long vehicleId,
            List<OrderInfo> orders,
            Map<String, String> trackingMap) {

        // 최대 적재량 조회
        Long maxLoad = reader.getVehicleMaxLoad(vehicleId);

        // 기존 적재량 조회
        Long currentWeight = reader.getCurrentTransitWeight(vehicleId);

        // 새로 배정할 주문들의 적재량 계산
        Long newWeight = orders.stream()
                .mapToLong(OrderInfo::weight)
                .sum();

        // 적재 가능 유효성 검증
        validator.checkLoadable(maxLoad, currentWeight, newWeight);

        // 차량 배정
        executor.createTransits(vehicleId, orders, trackingMap);
    }

    @Transactional
    public String cancelAssignment(Long transportId) {
        // 배정 정보 조회
        DeliverStatus status = reader.getTransitStatus(transportId);

        // 취소 가능 여부 검증
        validator.checkCancellable(status);

        // Transit 삭제 후 OrderCode 리턴
        return executor.cancelTransit(transportId);
    }
}
