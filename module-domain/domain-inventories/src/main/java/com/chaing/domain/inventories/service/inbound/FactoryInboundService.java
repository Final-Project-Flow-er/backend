package com.chaing.domain.inventories.service.inbound;

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
}
