package com.chaing.domain.inventories.usecase.executor;

import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.repository.FranchiseInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Qualifier("franchise")
@RequiredArgsConstructor
public class FranchiseExecutorImpl implements Executor<FranchiseInboundCreateCommand> {

    private final FranchiseInventoryRepository repository;

    @Override
    @Transactional
    public void create(FranchiseInboundCreateCommand command) {

        List<FranchiseInventory> inventories = command.serialCodes().stream()
                .map(serial -> FranchiseInventory.from(command, serial))
                .toList();

        repository.saveAll(inventories);
    }
}
