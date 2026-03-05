package com.chaing.domain.inventories.service.inbound;

import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
import com.chaing.domain.inventories.usecase.executor.Executor;
import com.chaing.domain.inventories.usecase.reader.Reader;
import com.chaing.domain.inventories.usecase.valiator.Validator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class InboundService<T> {

    protected final Reader reader;
    protected final Executor<T> executor;
    protected final Validator<T> validator;

    public void scanInbound(T command) {

        // 중복 스캔 여부 검증
        verifyDuplicate(command);

        // 데이터 정합성 검증
        verifyValidity(command);

        // 실제 제품 등록 및 입고 실행
        executor.create(command);
    }

    protected abstract void verifyDuplicate(T command);

    protected abstract void verifyValidity(T command);
}
