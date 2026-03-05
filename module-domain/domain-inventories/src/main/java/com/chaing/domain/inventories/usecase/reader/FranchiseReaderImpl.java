package com.chaing.domain.inventories.usecase.reader;

import com.chaing.domain.inventories.repository.FranchiseInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("franchise")
@RequiredArgsConstructor
public class FranchiseReaderImpl implements Reader {

    private final FranchiseInventoryRepository franchiseInventoryRepository;

    @Override
    public boolean existsBySerialCode(String serialCode) {

        return false;
    }
}
