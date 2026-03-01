package com.chaing.domain.transports.service;

import com.chaing.domain.transports.dto.request.TransportForceUpdateRequest;
import com.chaing.domain.transports.dto.request.VehicleAssignmentRequest;
import com.chaing.domain.transports.dto.response.AvailableVehicleResponse;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.usecase.executor.TransportExecutor;
import com.chaing.domain.transports.usecase.reader.TransportReader;
import com.chaing.domain.transports.usecase.validator.TransportValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalTransportService {

    private final TransportExecutor executor;
    private final TransportReader reader;
    private final TransportValidator validator;

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
}
