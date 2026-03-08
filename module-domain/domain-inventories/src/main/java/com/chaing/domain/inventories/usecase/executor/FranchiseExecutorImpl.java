package com.chaing.domain.inventories.usecase.executor;

import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.repository.FranchiseInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Component
@Qualifier("franchise")
@RequiredArgsConstructor
public class FranchiseExecutorImpl implements Executor<FranchiseInboundCreateCommand> {

    private final FranchiseInventoryRepository repository;

    @Override
    @Transactional
    public void create(FranchiseInboundCreateCommand command) {

        if (command.serialCodes().size() != command.orderItemIds().size()) {
            throw new InventoriesException(InventoriesErrorCode.INVENTORIES_UNMATCHED);
        }

        List<FranchiseInventory> inventories = IntStream.range(0, command.serialCodes().size())
                .mapToObj(i -> {
                    String serial = command.serialCodes().get(i);
                    Long orderItemId = command.orderItemIds().get(i); // i번째 아이템 ID 추출!

                    // 2. from 메서드에 orderItemId도 같이 넘겨주기
                    return FranchiseInventory.from(command, serial, orderItemId);
                })
                .toList();

        repository.saveAll(inventories);
    }

    @Override
    @Transactional
    public void confirmAll(List<String> confirmedIds) {
        repository.updateAllStatusInboundBySerialCode(confirmedIds);
    }
}
