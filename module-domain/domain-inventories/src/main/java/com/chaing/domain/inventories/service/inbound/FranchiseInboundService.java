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
            @Qualifier("franchise") Validator<FranchiseInboundCreateCommand, FranchiseInventory> validator
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
}
