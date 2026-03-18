package com.chaing.domain.inventories.usecase.outbound.executor;

import com.chaing.core.enums.LogType;

import java.util.List;

public interface OutboundExecutor {
    void updateAll(List<String> confirmedIds, LogType targetStatus);

    void assignBoxCode(String boxCode, Long orderId, Long orderItemId, List<String> confirmedIds);

    void cancelOutbound(List<String> confirmedIds);
}
