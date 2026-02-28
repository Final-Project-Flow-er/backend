package com.chaing.domain.transports.service;

import com.chaing.domain.transports.dto.request.TransportForceUpdateRequest;
import com.chaing.domain.transports.dto.request.VehicleAssignmentRequest;
import com.chaing.domain.transports.dto.response.AvailableVehicleResponse;
import com.chaing.domain.transports.usecase.executor.TransportExecutor;
import com.chaing.domain.transports.usecase.reader.TransportReader;
import com.chaing.domain.transports.usecase.validator.TransportValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalTransportService {

    private final TransportExecutor executor;
    private final TransportReader reader;
    private final TransportValidator validator;

    // 운송 가능 차량 리스트 조회
    @Transactional
    public AvailableVehicleResponse getAvailableVehicle() {
        var availableVehicles = reader.findAvailableVehicles();

        validator.validateListDispatchable(availableVehicles);
        validator.validateListUsableStatus(availableVehicles);
        validator.validateListWeightCapacity(availableVehicles);

        return AvailableVehicleResponse.from(availableVehicles);
    }

    // 차량 배정
    @Transactional
    public void assignVehicle(VehicleAssignmentRequest req) {

        var vehicle = reader.findVehicle(req.getVehicleId());

        validator.validateAvailableStatus(vehicle);

        executor.createTransit(req);

        // TODO: 1. AssignValidator로 배정 가능한 상태인지 검증 (운송장 존재 여부, 차량 가용 여부 등)
        // TODO: 2. TransportReader로 운송(Transport) 엔티티와 차량(Vehicle) 엔티티 읽어오기
        // TODO: 3. AssignExecutor로 실제 배정 로직 실행 (상태 변경 및 연관관계 매핑)
        // TODO: 4. (필요시) 결과 알림 이벤트 발행
    }

    // 배차 해제
    @Transactional
    public void cancelAssignment(Long transportId) {
        var transport = reader.getTransport(transportId);

        validator.validateForCancel(transport);

        executor.cancelAssignment(transport);
        // TODO: 1. CancelValidator로 해제 가능한 단계인지 확인 (이미 배송 시작됐으면 안 됨!)
        // TODO: 2. TransportReader로 운송 엔티티 조회
        // TODO: 3. CancelExecutor로 차량 연결 끊고 상태를 '미배정'으로 변경
    }

    // 입고 승인 시 상태 변경
    @Transactional
    public void approveArrival(Long transportId) {

        // TODO: 1. ArrivalValidator로 상태 확인
        // TODO: 2. TransportReader로 데이터 가져오기
        // TODO: 3. ArrivalExecutor로 도착 시간 기록 및 상태를 'ARRIVED'로 변경
    }

    // 상태 강제 수정
    @Transactional
    public void forceUpdateStatus(Long transportId, TransportForceUpdateRequest req) {

        var transport = reader.getTransport(transportId);

        validator.validatorForForceUpdate(transport, req.getTargetStatus());

        executor.forceUpdate(transport, req.getTargetStatus());

        // TODO: 1. ForceUpdateValidator - 수정 권한이 있는지, 변경하려는 상태값이 올바른지 검증
        // TODO: 2. TransportReader - 해당 운송 엔티티 가져오기
        // TODO: 3. ForceUpdateExecutor - 비즈니스 규칙 무시하고 전달받은 상태값으로 강제 업데이트!
        // TODO: 4. (선택사항) 운송 로그 남기기
    }
}
