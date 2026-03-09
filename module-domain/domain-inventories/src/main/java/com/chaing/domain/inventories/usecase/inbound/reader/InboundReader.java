package com.chaing.domain.inventories.usecase.inbound.reader;

import java.util.List;

public interface InboundReader<R> {
    boolean existsBySerialCode(String serialCode);

    List<R> findAllByStatusWait();

    List<R> findAllBySerialCode(List<String> selectedList);
}
