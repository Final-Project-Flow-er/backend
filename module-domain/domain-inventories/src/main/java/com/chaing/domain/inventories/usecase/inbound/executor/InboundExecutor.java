package com.chaing.domain.inventories.usecase.inbound.executor;

import java.util.List;

public interface InboundExecutor<T> {
    void create(T command);

    void confirmAll(List<String> confirmedIds);
}
