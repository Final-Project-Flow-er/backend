package com.chaing.domain.inventories.usecase.outbound.reader;

import com.chaing.domain.inventories.entity.FactoryInventory;

import java.util.List;

public interface OutboundReader {
    List<FactoryInventory> getAllBySerialCode(List<String> selectedList);
}
