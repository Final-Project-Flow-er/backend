package com.chaing.domain.inventories.usecase.reader;

import java.util.List;

public interface Reader<R> {
    boolean existsBySerialCode(String serialCode);

    List<R> findAllByStatusWait();

    List<R> findAllByIds(List<Long> selectedList);
}
