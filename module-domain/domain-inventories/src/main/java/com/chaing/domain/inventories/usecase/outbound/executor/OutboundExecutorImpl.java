package com.chaing.domain.inventories.usecase.outbound.executor;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.repository.FactoryInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboundExecutorImpl implements  OutboundExecutor {

    private final FactoryInventoryRepository repository;

    @Override
    public void updateAll(List<String> confirmedIds, LogType targetStatus) {
        repository.setTargetStatusBySerialCodeIn(confirmedIds, targetStatus);
    }
}
