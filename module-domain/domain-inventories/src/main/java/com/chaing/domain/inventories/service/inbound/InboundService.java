package com.chaing.domain.inventories.service.inbound;

import com.chaing.domain.inventories.dto.info.PendingBoxInfo;
import com.chaing.domain.inventories.dto.info.PendingItemInfo;
import com.chaing.domain.inventories.dto.raw.InboundRawData;
import com.chaing.domain.inventories.usecase.inbound.executor.InboundExecutor;
import com.chaing.domain.inventories.usecase.inbound.reader.InboundReader;
import com.chaing.domain.inventories.usecase.inbound.validator.InboundValidator;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class InboundService<T, R extends InboundRawData> {

    protected final InboundReader<R> inboundReader;
    protected final InboundExecutor<T> inboundExecutor;
    protected final InboundValidator<T, R> inboundValidator;

    // 입고 스캔
    public void scanInbound(T command) {

        // 중복 스캔 여부 검증
        verifyDuplicate(command);

        // 데이터 정합성 검증
        verifyValidity(command);

        // 실제 제품 등록 및 입고 실행
        inboundExecutor.create(command);
    }

    // 가맹점 박스 목록
    public List<PendingBoxInfo> getBoxInfos(Long franchiseId) {
        List<R> rawData = getRawPendingData();

        return rawData.stream()
                .filter(d -> isFranchise(d, franchiseId))
                .map(d -> PendingBoxInfo.fromBox((InboundRawData) d))
                .distinct()
                .toList();
    }

    // 가맹점 상세 목록
    public List<PendingItemInfo> getBoxDetails(String boxCode) {
        List<R> rawData = getRawPendingData();

        return rawData.stream()
                .filter(d -> d.getBoxCode().equals(boxCode))
                .map(d -> PendingItemInfo.fromItem((InboundRawData) d))
                .distinct()
                .toList();
    }

    // 공장 상세 목록
    public List<PendingItemInfo> getDetails() {
        List<R> rawData = getRawPendingData();

        return rawData.stream()
                .map(d -> PendingItemInfo.fromItem((InboundRawData) d))
                .distinct()
                .toList();
    }

    protected abstract void verifyDuplicate(T command);

    protected abstract void verifyValidity(T command);

    protected abstract List<R> getRawPendingData();

    protected abstract boolean isFranchise(R d, Long id);

    public abstract void confirmInbound(List<String> selectedList);
}
