package com.chaing.domain.inventories.usecase.reader;

import java.util.List;

public interface Reader<R> {
    boolean existsBySerialCode(String serialCode);

    List<R> findAllByStatusWait();
}
