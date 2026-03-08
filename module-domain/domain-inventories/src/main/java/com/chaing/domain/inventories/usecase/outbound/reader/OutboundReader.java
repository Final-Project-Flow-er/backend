package com.chaing.domain.inventories.usecase.outbound.reader;

import com.chaing.domain.inventories.entity.FactoryInventory;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboundReader {
    List<FactoryInventory> getAllBySerialCode(List<String> selectedList);

    @Query("select f from FactoryInventory f " +
            "where f.status in (com.chaing.core.enums.LogType.PICKING, com.chaing.core.enums.LogType.PICKING_WAIT)")

    List<FactoryInventory> getAllByBoxCodeAndStatus(String boxCode);
}
