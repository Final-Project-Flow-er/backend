package com.chaing.api.facade.transport;

import com.chaing.api.dto.transport.management.request.CreateVehicleRequest;
import com.chaing.api.dto.transport.management.request.UpdateVehicleRequest;
import com.chaing.api.dto.transport.management.request.UpdateVehicleStatusRequest;
import com.chaing.api.dto.transport.management.response.VehicleDetailResponse;
import com.chaing.api.dto.transport.management.response.VehicleSummaryResponse;
import com.chaing.domain.transports.entity.Vehicle;
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

    // 운송 차량 등록
    @Transactional
    public VehicleDetailResponse createVehicle(CreateVehicleRequest request) {
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
    @Transactional
    public VehicleDetailResponse updateVehicle(Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = vehicleManagementService.updateVehicle(id, request.toCommand());
        return VehicleDetailResponse.from(vehicle);
    }

    // 운송 차량 상태 변경
    @Transactional
    public VehicleDetailResponse updateVehicleStatus(Long id, UpdateVehicleStatusRequest request) {
        Vehicle vehicle = vehicleManagementService.updateStatus(id, request.status());
        return VehicleDetailResponse.from(vehicle);
    }

    // 운송 차량 삭제
    @Transactional
    public void deleteVehicle(Long id) {
        vehicleManagementService.deleteVehicle(id);
    }
}
