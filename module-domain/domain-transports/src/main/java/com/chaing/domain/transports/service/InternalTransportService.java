package com.chaing.domain.transports.service;

import com.chaing.domain.transports.dto.DeliveryFeeInfo;
import com.chaing.domain.transports.dto.OrderInfo;
import com.chaing.domain.transports.dto.response.AvailableVehicleInfo;
import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.usecase.executor.TransportExecutor;
import com.chaing.domain.transports.usecase.reader.TransportReader;
import com.chaing.domain.transports.usecase.validator.TransportValidator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Validated
@Transactional(readOnly = true)
public class InternalTransportService {

    private final TransportExecutor executor;
    private final TransportReader reader;
    private final TransportValidator validator;

    // 운송 가능 차량 목록 조회
    public List<AvailableVehicleInfo> getAvailableVehicle() {

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
                    String transportName = reader.getTransportName(vehicle.getTransportId());
                    return AvailableVehicleInfo.from(transportName, vehicle, currentWeight);
                })
                .toList();
    }

    // 차량 배정
    @Transactional
    public void assignVehicle(
            @NotNull(message = "차량을 선택해주세요") Long vehicleId,
            List<OrderInfo> orders,
            Map<String, String> trackingMap,
            Long newWeight) {

        List<String> returnCodes = List.of();

        // 최대 적재량 조회
        Long maxLoad = reader.getVehicleMaxLoad(vehicleId);

        // 기존 적재량 조회
        Long currentWeight = reader.getCurrentTransitWeight(vehicleId);

        // 적재 가능 유효성 검증
        validator.checkLoadable(maxLoad, currentWeight, newWeight);

        // 송장 유효성 검증
        validator.checkTrackingNumber(orders, trackingMap);

        // 차량 배정
        executor.createTransits(vehicleId, orders, trackingMap, returnCodes);

        // 차량 상태 확인 및 변경
        if(maxLoad < currentWeight + newWeight + 100) {
            executor.updateDispatchableStatus(vehicleId);
        }
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


    public List<DeliveryFeeInfo> calculateDeliveryFee(List<OrderInfo> orderInfos, @NotNull(message = "차량을 선택해주세요") Long vehicleId) {

        List<Long> franchiseList = orderInfos.stream()
                .map(OrderInfo::franchiseId)
                .distinct()
                .toList();

        BigDecimal deliveryFee = BigDecimal.valueOf(reader.getDeliveryFee(vehicleId));

        return franchiseList.stream()
                .map(franchiseId -> new DeliveryFeeInfo(franchiseId, deliveryFee))
                .toList();
    }

    public List<AvailableVehicleInfo> getAllAvailableVehicle() {
        List <Vehicle> vehicleList = reader.getAllAvailableVehicles();

        return vehicleList.stream()
                .map(vehicle -> {
                    Long currentWeight = reader.getCurrentTransitWeight(vehicle.getVehicleId());
                    String transportName = reader.getTransportName(vehicle.getTransportId());
                    return AvailableVehicleInfo.from(transportName, vehicle, currentWeight);
                }).toList();
    }

    public void assignVehicleReturn(@NotNull(message = "차량을 선택해주세요") Long vehicleId,
                                    List<OrderInfo> orderInfos, Map<String, String> trackingMap,
                                    Long totalWeight, List<String> returnCodes) {

        // 송장 유효성 검증
        validator.checkTrackingNumber(orderInfos, trackingMap);

        // 차량 배정
        executor.createTransits(vehicleId, orderInfos, trackingMap, returnCodes);
    }

    public void updateDeliveryStatus(List<String> orderCodes) {

        if(orderCodes == null || orderCodes.isEmpty()) {
            throw new TransportException(TransportErrorCode.TRANSPORT_NOT_FOUND);
        }

        List<Transit> transitInfo = reader.getTransitInfo(orderCodes);

        transitInfo.forEach(transit -> {
            DeliverStatus currentStatus = transit.getStatus();
            String orderCode = transit.getOrderCode();

            DeliverStatus targetStatus = switch (currentStatus) {
                case PENDING -> DeliverStatus.IN_TRANSIT;
                case IN_TRANSIT -> DeliverStatus.DELIVERED;
                default -> throw new TransportException(TransportErrorCode.TRANSPORT_CAN_NOT_CANCEL);
            };

            executor.updateDeliverStatus(orderCode, targetStatus);
        });

    }

    public boolean filterPendingOrders(@NotEmpty List<String> orderCodes) {
        return reader.getTransitInfo(orderCodes)
                .stream()
                .allMatch(transit -> transit.getStatus() == DeliverStatus.PENDING);
    }
}
