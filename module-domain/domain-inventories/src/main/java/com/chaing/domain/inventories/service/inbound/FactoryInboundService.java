package com.chaing.domain.inventories.service.inbound;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.command.FactoryInboundCreateCommand;
import com.chaing.domain.inventories.dto.raw.FactoryInventoryRawData;
import com.chaing.domain.inventories.usecase.executor.Executor;
import com.chaing.domain.inventories.usecase.reader.Reader;
import com.chaing.domain.inventories.usecase.valiator.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FactoryInboundService extends InboundService<FactoryInboundCreateCommand, FactoryInventoryRawData> {

    public FactoryInboundService(
            @Qualifier("factory") Reader<FactoryInventoryRawData> reader,
            @Qualifier("factory") Executor<FactoryInboundCreateCommand> executor,
            @Qualifier("factory") Validator<FactoryInboundCreateCommand, FactoryInventoryRawData> validator
    ) {
        super(reader, executor, validator);
    }

    @Override
    protected void verifyDuplicate(FactoryInboundCreateCommand command) {
        validator.checkAlreadyScanned(command.serialCode());
    }

    @Override
    protected void verifyValidity(FactoryInboundCreateCommand command) {
        validator.checkScanValidity(command);
    }

    @Override
    protected List<FactoryInventoryRawData> getRawPendingData() {
        List<FactoryInventoryRawData> entities = reader.findAllByStatusWait();

        validator.checkPendingDataExistence(entities);

        return entities;
    }

    @Override
    protected boolean isFranchise(FactoryInventoryRawData d, Long id) {
        return true;
    }

    @Override
    public void confirmInbound(List<Long> selectedList) {
        // 조회
        List<FactoryInventoryRawData> targets = reader.findAllByIds(selectedList);

        // 누락된 값 존재 여부 검증
        validator.checkPendingDataExistence(targets);

        // 상태 INBOUND_WAIT 검증
        List<LogType> statuses = targets.stream()
                        .map(FactoryInventoryRawData::status)
                        .toList();
        validator.checkValidStatus(statuses);

        // 검증이 완료된 정보들의 식별 코드 추출
        List<String> confirmedIds = targets.stream()
                .map(FactoryInventoryRawData::serialCode)
                .toList();

        // 상태 INBOUND로 변경(승인 확정)
        executor.confirmAll(confirmedIds);
    }
}
