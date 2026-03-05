package com.chaing.domain.inventories.service.inbound;

import com.chaing.domain.inventories.dto.command.FactoryInboundCreateCommand;
import com.chaing.domain.inventories.usecase.executor.Executor;
import com.chaing.domain.inventories.usecase.reader.Reader;
import com.chaing.domain.inventories.usecase.valiator.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class FactoryInboundService extends InboundService<FactoryInboundCreateCommand> {

    public FactoryInboundService(
            @Qualifier("factory") Reader reader,
            @Qualifier("factory") Executor executor,
            @Qualifier("factory") Validator validator
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
}
