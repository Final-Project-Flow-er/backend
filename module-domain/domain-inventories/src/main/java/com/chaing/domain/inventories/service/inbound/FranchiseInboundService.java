package com.chaing.domain.inventories.service.inbound;

import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
import com.chaing.domain.inventories.usecase.executor.Executor;
import com.chaing.domain.inventories.usecase.reader.Reader;
import com.chaing.domain.inventories.usecase.valiator.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class FranchiseInboundService extends InboundService<FranchiseInboundCreateCommand> {

    public FranchiseInboundService(
            @Qualifier("franchise") Reader reader,
            @Qualifier("franchise") Executor executor,
            @Qualifier("franchise") Validator validator
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
}
