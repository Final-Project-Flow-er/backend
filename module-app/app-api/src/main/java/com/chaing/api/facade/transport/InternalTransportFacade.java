package com.chaing.api.facade.transport;

import com.chaing.domain.transports.dto.response.AvailableVehicleResponse;
import com.chaing.domain.transports.service.InternalTransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalTransportFacade {

    private final InternalTransportService transportService;

    // 운송 가능 차량 리스트 조회
    public List<AvailableVehicleResponse> getAvailableVehicle() {

        return transportService.getAvailableVehicle();
    }


}
