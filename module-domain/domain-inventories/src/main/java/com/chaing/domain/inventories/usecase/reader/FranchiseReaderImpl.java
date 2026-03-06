package com.chaing.domain.inventories.usecase.reader;

import com.chaing.domain.inventories.dto.raw.FranchiseInventoryRawData;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.repository.FranchiseInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Qualifier("franchise")
@RequiredArgsConstructor
public class FranchiseReaderImpl implements Reader<FranchiseInventoryRawData> {

    private final FranchiseInventoryRepository repository;

    @Override
    public boolean existsBySerialCode(String serialCode) {
        return false;
    }

    @Override
    public List<FranchiseInventoryRawData> findAllByStatusWait() {

        List<FranchiseInventory> entities = repository.findAllByStatusInboundWait();

        return entities.stream()
                .map(entity -> new FranchiseInventoryRawData(
                        entity.getBoxCode(),
                        entity.getProductId(),
                        entity.getSerialCode(),
                        entity.getManufactureDate(),
                        entity.getFranchiseId(),
                        entity.getStatus()
                ))
                .toList();
    }
}
