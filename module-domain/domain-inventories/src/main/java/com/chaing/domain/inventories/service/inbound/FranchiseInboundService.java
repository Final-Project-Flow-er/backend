package com.chaing.domain.inventories.service.inbound;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
import com.chaing.domain.inventories.dto.raw.FranchiseInventoryRawData;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.usecase.executor.Executor;
import com.chaing.domain.inventories.usecase.reader.Reader;
import com.chaing.domain.inventories.usecase.valiator.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FranchiseInboundService extends InboundService<FranchiseInboundCreateCommand, FranchiseInventoryRawData> {

    public FranchiseInboundService(
            @Qualifier("franchise") Reader<FranchiseInventoryRawData> reader,
            @Qualifier("franchise") Executor<FranchiseInboundCreateCommand> executor,
            @Qualifier("franchise") Validator<FranchiseInboundCreateCommand, FranchiseInventoryRawData> validator
            ) {
        super(reader, executor, validator);
    }

    @Override
    protected void verifyDuplicate(FranchiseInboundCreateCommand command) {
        command.serialCodes().forEach(validator::checkAlreadyScanned);
    }

    @Override
    protected void verifyValidity(FranchiseInboundCreateCommand command) {
        validator.checkScanValidity(command);
    }

    @Override
    protected List<FranchiseInventoryRawData> getRawPendingData() {

        List<FranchiseInventoryRawData> entities = reader.findAllByStatusWait();

        validator.checkPendingDataExistence(entities);

        return entities;
    }

    @Override
    protected boolean isFranchise(FranchiseInventoryRawData data, Long id) {
        return data.franchiseId().equals(id);
    }

    @Override
    public void confirmInbound(List<Long> selectedList) {
        // 조회
        List<FranchiseInventoryRawData> targets = reader.findAllByIds(selectedList);

        // 누락된 값 존재 여부 검증
        validator.checkPendingDataExistence(targets);

        // 상태 INBOUND_WAIT 검증
        List<LogType> statuses = targets.stream()
                .map(FranchiseInventoryRawData::status)
                .toList();
        validator.checkValidStatus(statuses);

        // 검증이 완료된 정보들의 id 추출
        List<String> confirmedIds = targets.stream()
                .map(FranchiseInventoryRawData::serialCode)
                .toList();

        // 상태 INBOUND로 변경(승인 확정)
        executor.confirmAll(confirmedIds);
    }
}
