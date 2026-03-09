package com.chaing.domain.inventories.service.inbound;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
import com.chaing.domain.inventories.dto.raw.FranchiseInventoryRawData;
import com.chaing.domain.inventories.usecase.inbound.executor.InboundExecutor;
import com.chaing.domain.inventories.usecase.inbound.reader.InboundReader;
import com.chaing.domain.inventories.usecase.inbound.validator.InboundValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FranchiseInboundService extends InboundService<FranchiseInboundCreateCommand, FranchiseInventoryRawData> {

    public FranchiseInboundService(
            @Qualifier("franchise") InboundReader<FranchiseInventoryRawData> inboundReader,
            @Qualifier("franchise") InboundExecutor<FranchiseInboundCreateCommand> inboundExecutor,
            @Qualifier("franchise") InboundValidator<FranchiseInboundCreateCommand, FranchiseInventoryRawData> inboundValidator
            ) {
        super(inboundReader, inboundExecutor, inboundValidator);
    }

    @Override
    protected void verifyDuplicate(FranchiseInboundCreateCommand command) {
        command.serialCodes().forEach(inboundValidator::checkAlreadyScanned);
    }

    @Override
    protected void verifyValidity(FranchiseInboundCreateCommand command) {
        inboundValidator.checkScanValidity(command);
    }

    @Override
    protected List<FranchiseInventoryRawData> getRawPendingData() {

        List<FranchiseInventoryRawData> entities = inboundReader.findAllByStatusWait();

        inboundValidator.checkPendingDataExistence(entities);

        return entities;
    }

    @Override
    protected boolean isFranchise(FranchiseInventoryRawData data, Long id) {
        return data.franchiseId().equals(id);
    }

    @Override
    public void confirmInbound(List<String> selectedList) {
        // 조회
        List<FranchiseInventoryRawData> targets = inboundReader.findAllBySerialCode(selectedList);

        // 누락된 값 존재 여부 검증
        inboundValidator.checkPendingDataExistence(targets);

        // 상태 INBOUND_WAIT 검증
        List<LogType> statuses = targets.stream()
                .map(FranchiseInventoryRawData::status)
                .toList();
        inboundValidator.checkValidStatus(statuses);

        // 검증이 완료된 정보들의 id 추출
        List<String> confirmedIds = targets.stream()
                .map(FranchiseInventoryRawData::serialCode)
                .toList();

        // 상태 INBOUND로 변경(승인 확정)
        inboundExecutor.confirmAll(confirmedIds);
    }
}
