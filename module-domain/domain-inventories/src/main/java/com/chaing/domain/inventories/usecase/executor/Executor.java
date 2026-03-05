package com.chaing.domain.inventories.usecase.executor;

public interface Executor<T> {
    void create(T command);
}
