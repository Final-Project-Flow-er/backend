package com.chaing.api.facade.transport;

import com.chaing.api.dto.transport.management.request.CreateTransportRequest;
import com.chaing.api.dto.transport.management.request.UpdateTransportRequest;
import com.chaing.api.dto.transport.management.request.UpdateTransportStatusRequest;
import com.chaing.api.dto.transport.management.response.TransportDetailResponse;
import com.chaing.api.dto.transport.management.response.TransportSummaryResponse;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Transport;
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
public class TransportManagementFacade {

    private final TransportManagementService transportManagementService;
    private final VehicleManagementService vehicleManagementService;

    // 운송 업체 등록
    @Transactional
    public TransportDetailResponse createTransport(CreateTransportRequest request) {
        Transport transport = transportManagementService.createTransport(request.toCommand());
        return TransportDetailResponse.from(transport);
    }

    // 운송 업체 목록 조회
    public Page<TransportSummaryResponse> getTransportList(Pageable pageable) {
        Page<Transport> transports = transportManagementService.getTransportList(pageable);
        return transports.map(TransportSummaryResponse::from);
    }

    // 운송 업체 상세 조회
    public TransportDetailResponse getTransportDetail(Long id) {
        Transport transport = transportManagementService.getById(id);
        return TransportDetailResponse.from(transport);
    }

    // 운송 업체 수정
    @Transactional
    public TransportDetailResponse updateTransport(Long id, UpdateTransportRequest request) {
        Transport transport = transportManagementService.updateTransport(id, request.toCommand());
        return TransportDetailResponse.from(transport);
    }

    // 운송 업체 상태 변경
    @Transactional
    public TransportDetailResponse updateTransportStatus(Long id, UpdateTransportStatusRequest request) {
        Transport transport = transportManagementService.updateStatus(id, request.status());

        if (request.status() == UsableStatus.INACTIVE) {
            vehicleManagementService.deactivateVehiclesByTransportId(id);
        }

        return TransportDetailResponse.from(transport);
    }

    // 운송 업체 삭제
    @Transactional
    public void deleteTransport(Long id) {
        transportManagementService.deleteTransport(id);
        vehicleManagementService.deleteVehiclesByTransportId(id);
    }
}
