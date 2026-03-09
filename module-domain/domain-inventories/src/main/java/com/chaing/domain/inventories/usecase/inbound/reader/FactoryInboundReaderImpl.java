package com.chaing.domain.inventories.usecase.inbound.reader;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.raw.FactoryInventoryRawData;
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.repository.FactoryInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Qualifier("factory")
@RequiredArgsConstructor
public class FactoryInboundReaderImpl implements InboundReader<FactoryInventoryRawData> {

    private final FactoryInventoryRepository repository;
    @Override
    public boolean existsBySerialCode(String serialCode) {
        return false;
    }

    @Override
    public List<FactoryInventoryRawData> findAllByStatusWait() {

        List<FactoryInventory> entities = repository.findAllByStatus(LogType.INBOUND_WAIT);

        return entities.stream()
                .map(entity -> new FactoryInventoryRawData(
                        entity.getProductId(),
                        entity.getSerialCode(),
                        entity.getManufactureDate(),
                        entity.getStatus()
                ))
                .toList();
    }

    @Override
    public List<FactoryInventoryRawData> findAllBySerialCode(List<String> selectedList) {
        List<FactoryInventory> entities = repository.findAllBySerialCodeIn(selectedList);

        return entities.stream()
                .map(entity -> new FactoryInventoryRawData(
                        entity.getProductId(),
                        entity.getSerialCode(),
                        entity.getManufactureDate(),
                        entity.getStatus()
                ))
                .toList();
    }
}
