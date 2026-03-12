package com.chaing.api.facade.transport;

import com.chaing.api.dto.transport.management.request.CreateVehicleRequest;
import com.chaing.api.dto.transport.management.request.UpdateVehicleRequest;
import com.chaing.api.dto.transport.management.request.UpdateVehicleStatusRequest;
import com.chaing.api.dto.transport.management.response.VehicleDetailResponse;
import com.chaing.api.dto.transport.management.response.VehicleSummaryResponse;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Transport;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.service.TransportManagementService;
import com.chaing.domain.transports.service.VehicleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleManagementFacade {

    private final VehicleManagementService vehicleManagementService;
    private final TransportManagementService transportManagementService;

    // 운송 차량 등록
    @Transactional(rollbackFor = Exception.class)
    public VehicleDetailResponse createVehicle(CreateVehicleRequest request) {
        validateTransportIsActive(request.transportId());
        Vehicle vehicle = vehicleManagementService.createVehicle(request.toCommand());
        return VehicleDetailResponse.from(vehicle);
    }

    // 운송 차량 목록 조회
    public Page<VehicleSummaryResponse> getVehicleList(Pageable pageable) {
        Page<Vehicle> vehicles = vehicleManagementService.getVehicleList(pageable);
        return vehicles.map(VehicleSummaryResponse::from);
    }

    // 운송 차량 상세 조회
    public VehicleDetailResponse getVehicleDetail(Long id) {
        Vehicle vehicle = vehicleManagementService.getById(id);
        return VehicleDetailResponse.from(vehicle);
    }

    // 운송 차량 수정
    @Transactional(rollbackFor = Exception.class)
    public VehicleDetailResponse updateVehicle(Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = vehicleManagementService.getById(id);

        if (request.status() == UsableStatus.ACTIVE) {
            validateTransportIsActive(vehicle.getTransportId());
        }
        if (request.transportId() != null && !request.transportId().equals(vehicle.getTransportId())) {
            validateTransportIsActive(request.transportId());
        }

        Vehicle updatedVehicle = vehicleManagementService.updateVehicle(id, request.toCommand());
        return VehicleDetailResponse.from(updatedVehicle);
    }

    // 운송 차량 상태 변경
    @Transactional(rollbackFor = Exception.class)
    public VehicleDetailResponse updateVehicleStatus(Long id, UpdateVehicleStatusRequest request) {
        if (request.status() == UsableStatus.ACTIVE) {
            Vehicle vehicle = vehicleManagementService.getById(id);
            validateTransportIsActive(vehicle.getTransportId());
        }

        Vehicle vehicle = vehicleManagementService.updateStatus(id, request.status());
        return VehicleDetailResponse.from(vehicle);
    }

    // 운송 차량 삭제
    @Transactional(rollbackFor = Exception.class)
    public void deleteVehicle(Long id) {
        vehicleManagementService.deleteVehicle(id);
    }

    // 운송 업체가 활성화 상태인지 검증
    private void validateTransportIsActive(Long transportId) {
        Transport transport = transportManagementService.getById(transportId);
        if (transport.getStatus() == UsableStatus.INACTIVE) {
            throw new TransportException(TransportErrorCode.INACTIVE_TRANSPORT_VEHICLE);
        }
    }
}
