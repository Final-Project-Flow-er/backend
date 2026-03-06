package com.chaing.domain.inventories.usecase.executor;

import java.util.List;

public interface Executor<T> {
    void create(T command);

    void confirmAll(List<String> confirmedIds);
}
