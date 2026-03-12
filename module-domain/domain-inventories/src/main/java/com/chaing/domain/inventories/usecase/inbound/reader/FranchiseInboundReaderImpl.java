package com.chaing.domain.inventories.usecase.inbound.reader;

import com.chaing.core.enums.LogType;
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
public class FranchiseInboundReaderImpl implements InboundReader<FranchiseInventoryRawData> {

    private final FranchiseInventoryRepository repository;

    @Override
    public boolean existsBySerialCode(String serialCode) {
        return false;
    }

    @Override
    public List<FranchiseInventoryRawData> findAllByStatusWait() {

        List<FranchiseInventory> entities = repository.findAllByStatus(LogType.INBOUND_WAIT);

        return entities.stream()
                .map(entity -> new FranchiseInventoryRawData(
                        entity.getBoxCode(),
                        entity.getProductId(),
                        entity.getSerialCode(),
                        entity.getManufactureDate(),
                        entity.getFranchiseId(),
                        entity.getOrderId(),
                        entity.getOrderItemId(),
                        entity.getStatus()
                ))
                .toList();
    }

    @Override
    public List<FranchiseInventoryRawData> findAllBySerialCode(List<String> selectedList) {
        List<FranchiseInventory> entities = repository.findAllBySerialCodeIn(selectedList);

        return entities.stream()
                .map(entity -> new FranchiseInventoryRawData(
                        entity.getBoxCode(),
                        entity.getProductId(),
                        entity.getSerialCode(),
                        entity.getManufactureDate(),
                        entity.getFranchiseId(),
                        entity.getOrderId(),
                        entity.getOrderItemId(),
                        entity.getStatus()
                ))
                .toList();
    }

    @Override
    public List<FranchiseInventoryRawData> findAllByBoxCode(String boxCode) {

        List<FranchiseInventory> entities = repository.findAllByBoxCode(boxCode);

        return entities.stream()
                .map(entity -> new FranchiseInventoryRawData(
                        entity.getBoxCode(),
                        entity.getProductId(),
                        entity.getSerialCode(),
                        entity.getManufactureDate(),
                        entity.getFranchiseId(),
                        entity.getOrderId(),
                        entity.getOrderItemId(),
                        entity.getStatus()
                ))
                .toList();
    }
}
