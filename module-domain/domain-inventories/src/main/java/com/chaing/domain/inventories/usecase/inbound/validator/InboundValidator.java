package com.chaing.domain.inventories.usecase.inbound.validator;

import com.chaing.core.enums.LogType;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface InboundValidator<T, R> {

    void checkAlreadyScanned(@NotBlank String serialCode);

    void checkScanValidity(T command);

    void checkPendingDataExistence(List<R> entities);

    void checkValidStatus(List<LogType> statuses);
}
