package com.chaing.domain.inventories.usecase.outbound.reader;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.repository.FactoryInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboundReaderImpl implements OutboundReader{

    private final FactoryInventoryRepository repository;

    @Override
    public List<FactoryInventory> getAllBySerialCode(List<String> selectedList) {
        return repository.findAllBySerialCodeIn(selectedList);
    }

    @Override
    public List<FactoryInventory> getAllByBoxCodeAndStatus(String boxCode) {
        LogType permittedStatus1 = LogType.PICKING;
        LogType permittedStatus2 = LogType.PICKING_WAIT;
        return repository.findAllByBoxCodeAndStatuses(boxCode, permittedStatus1, permittedStatus2);    }
}
