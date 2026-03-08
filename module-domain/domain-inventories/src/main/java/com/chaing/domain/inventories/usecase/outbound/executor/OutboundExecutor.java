package com.chaing.domain.inventories.usecase.outbound.executor;

import com.chaing.core.enums.LogType;

import java.util.List;

public interface OutboundExecutor {
    void updateAll(List<String> confirmedIds, LogType targetStatus);

    void assignBoxCode(String boxCode, List<String> confirmedIds);
}
