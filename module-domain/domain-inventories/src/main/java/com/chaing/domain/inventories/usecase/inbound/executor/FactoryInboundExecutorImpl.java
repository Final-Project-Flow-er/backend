package com.chaing.domain.inventories.usecase.inbound.executor;

import com.chaing.domain.inventories.dto.command.FactoryInboundCreateCommand;
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.repository.FactoryInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Qualifier("factory")
@RequiredArgsConstructor
public class FactoryInboundExecutorImpl implements InboundExecutor<FactoryInboundCreateCommand> {

    private final FactoryInventoryRepository repository;

    @Override
    @Transactional
    public void create(FactoryInboundCreateCommand command) {

        FactoryInventory factoryInbound = FactoryInventory.from(command);

        repository.save(factoryInbound);
    }

    @Override
    @Transactional
    public void confirmAll(List<String> confirmedIds) {
        repository.updateAllStatusAvailableBySerialCode(confirmedIds);
    }
}
