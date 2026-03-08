package com.chaing.domain.inventories.usecase.outbound.reader;

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
}
