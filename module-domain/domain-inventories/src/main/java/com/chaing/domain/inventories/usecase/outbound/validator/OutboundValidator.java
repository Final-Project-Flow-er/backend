package com.chaing.domain.inventories.usecase.outbound.validator;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.FactoryInventory;

import java.util.List;

public interface OutboundValidator {
    void checkPendingDataExistence(List<FactoryInventory> targets);

    void checkValidStatus(LogType status, LogType logType);

    void checkBoxCode(String targetBoxCode);

    void isTargetMatched(String boxCode, String targetBoxCode);
}
