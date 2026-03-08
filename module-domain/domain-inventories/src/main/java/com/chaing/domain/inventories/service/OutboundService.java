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
    public void updateStatus(List<String> selectedList) {

        // 조회
        List<FactoryInventory> targets = outboundReader.getAllBySerialCode(selectedList);

        // 누락된 값 존재 여부 검증
        outboundValidator.checkPendingDataExistence(targets);

        AtomicReference<LogType> targetStatus = new AtomicReference<>(LogType.AVAILABLE);

        // 상태 분기 및 검증
        targets.forEach(target -> {
            LogType status = target.getStatus();
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
}
