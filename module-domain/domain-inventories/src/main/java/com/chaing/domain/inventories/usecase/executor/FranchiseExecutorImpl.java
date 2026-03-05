package com.chaing.domain.inventories.usecase.executor;

import com.chaing.domain.inventories.dto.command.FactoryInboundCreateCommand;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.repository.FranchiseInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Qualifier("franchise")
@RequiredArgsConstructor
public class FranchiseExecutorImpl implements Executor<FactoryInboundCreateCommand> {

    private final FranchiseInventoryRepository repository;

    @Override
    @Transactional
    public void create(FactoryInboundCreateCommand command) {

        FranchiseInventory franchiseInbound = FranchiseInventory.from(command);

        repository.save(franchiseInbound);
    }
}
