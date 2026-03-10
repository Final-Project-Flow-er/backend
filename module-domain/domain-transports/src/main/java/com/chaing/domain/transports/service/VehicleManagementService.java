package com.chaing.domain.transports.service;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.command.VehicleCreateCommand;
import com.chaing.domain.transports.dto.command.VehicleUpdateCommand;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleManagementService {

    private final VehicleRepository vehicleRepository;

    // 운송 차량 등록
    public Vehicle createVehicle(VehicleCreateCommand command) {
        Vehicle vehicle = Vehicle.createVehicle(command);
        return vehicleRepository.save(vehicle);
    }

    // 운송 차량 목록 조회
    public Page<Vehicle> getVehicleList(Pageable pageable) {
        return vehicleRepository.findAll(pageable);
    }

    // 운송 차량 상세 조회
    public Vehicle getById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new TransportException(TransportErrorCode.TRANSPORT_VEHICLE_NOT_FOUND));
    }

    // 운송 차량 수정
    public Vehicle updateVehicle(Long id, VehicleUpdateCommand command) {
        Vehicle vehicle = getById(id);
        vehicle.updateVehicle(command);
        return vehicle;
    }

    // 운송 차량 상태 변경
    public Vehicle updateStatus(Long id, UsableStatus status) {
        Vehicle vehicle = getById(id);
        vehicle.updateStatus(status);
        return vehicle;
    }

    // 운송 차량 삭제
    public void deleteVehicle(Long id) {
        Vehicle vehicle = getById(id);
        vehicle.delete();
    }

    // 운송 업체 상태 변경 시 소속 차량 일괄 상태 변경
    public void deactivateVehiclesByTransportId(Long transportId, UsableStatus status) {
        vehicleRepository.updateStatusByTransportId(transportId, status);
    }

    // 운송 업체 삭제 시 소속 차량 일괄 삭제
    public void deleteVehiclesByTransportId(Long transportId) {
        vehicleRepository.deleteVehiclesByTransportId(transportId);
    }
}
