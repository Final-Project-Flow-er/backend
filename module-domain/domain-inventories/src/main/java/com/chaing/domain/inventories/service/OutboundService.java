package com.chaing.domain.inventories.service;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.usecase.outbound.executor.OutboundExecutor;
import com.chaing.domain.inventories.usecase.outbound.reader.OutboundReader;
import com.chaing.domain.inventories.usecase.outbound.validator.OutboundValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboundService {

    private final OutboundReader outboundReader;
    private final OutboundValidator outboundValidator;
    private final OutboundExecutor outboundExecutor;

    @Transactional
    public void updateStatus(List<String> selectedList, LogType currentStatus) {

        // 조회
        List<FactoryInventory> targets = getListAndValidate(selectedList);

        AtomicReference<LogType> targetStatus = new AtomicReference<>(LogType.AVAILABLE);

        // 상태 분기 및 검증
        targets.forEach(target -> {
            LogType status = target.getStatus();
            if (status != currentStatus) {
                throw new InventoriesException(InventoriesErrorCode.INVALID_OUTBOUND_STATUS);
            }
            switch (status) {
                case AVAILABLE:
                    outboundValidator.checkValidStatus(status, LogType.AVAILABLE);
                    targetStatus.set(LogType.PICKING_WAIT);
                    break;
                case PICKING_WAIT:
                    outboundValidator.checkValidStatus(status, LogType.PICKING_WAIT);
                    outboundValidator.checkBoxCode(target.getBoxCode());
                    targetStatus.set(LogType.PICKING);
                    break;
                case PICKING:
                    outboundValidator.checkValidStatus(status, LogType.PICKING);
                    outboundValidator.checkBoxCode(target.getBoxCode());
                    targetStatus.set(LogType.OUTBOUND);
                    break;
                default:
                    throw new InventoriesException(InventoriesErrorCode.INVALID_OUTBOUND_STATUS);
            }
        });

        // 검증이 완료된 정보들의 ID 추출
        List<String> confirmedIds = targets.stream()
                .map(FactoryInventory::getSerialCode)
                .toList();

        // 상태 변경 실행
        outboundExecutor.updateAll(confirmedIds, targetStatus.get());
    }

    // 박스 할당
    @Transactional
    public void assignBox(String boxCode, List<String> serialCodes) {
        List<FactoryInventory> selectedList = getListAndValidate(serialCodes);

        List<String> confirmedIds = selectedList.stream()
                .map(FactoryInventory::getSerialCode)
                .toList();

        outboundExecutor.assignBoxCode(boxCode, confirmedIds);
    }

    // 할당 취소 및 출고 취소
    @Transactional
    public void cancelOutbound(String boxCode, List<String> serialCodes) {
        List<FactoryInventory> selectedList = getListAndValidate(serialCodes);

        // 타겟 리스트가 가용 상태가 아닌지 확인
        selectedList.forEach(target -> {
            LogType status = target.getStatus();

            switch (status) {
                case PICKING_WAIT:
                case PICKING:
                case OUTBOUND:
                    break;
                default:
                    throw new  InventoriesException(InventoriesErrorCode.INVALID_OUTBOUND_CANCEL_STATUS);
            }
        });

        // 검증된 값 시리얼 코드 추출
        List<String> confirmedIds = selectedList.stream()
                .map(FactoryInventory::getSerialCode)
                .toList();

        // 박스 코드 할당 취소 및 상태 변경
        outboundExecutor.cancelOutbound(confirmedIds);
    }

    public List<FactoryInventory> getListAndValidate(List<String> serialCodes) {
        // 조회
        List<FactoryInventory> targets = outboundReader.getAllBySerialCode(serialCodes);

        // 누락된 값 존재 여부 검증
        outboundValidator.checkPendingDataExistence(targets);

        return targets;
    }
}
