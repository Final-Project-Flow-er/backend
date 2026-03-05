package com.chaing.domain.inventories.usecase.reader;

import com.chaing.domain.inventories.repository.FactoryInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("factory")
@RequiredArgsConstructor
public class FactoryReaderImpl implements Reader {

    private final FactoryInventoryRepository repository;
    @Override
    public boolean existsBySerialCode(String serialCode) {
        return false;
    }
}
