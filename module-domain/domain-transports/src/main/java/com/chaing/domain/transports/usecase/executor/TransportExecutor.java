package com.chaing.domain.transports.usecase.executor;

import com.chaing.domain.transports.dto.OrderInfo;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public interface TransportExecutor {
    void createTransits(@NotNull(message = "차량을 선택해주세요") Long vehicleId, List<OrderInfo> orders, Map<String, String> trackingMap, List<String> returnCodes);

    String cancelTransit(Long transportId);

    void updateDispatchableStatus(Long vehicleId);
}
