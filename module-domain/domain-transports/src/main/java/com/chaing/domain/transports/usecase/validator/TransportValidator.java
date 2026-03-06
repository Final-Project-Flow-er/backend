package com.chaing.domain.transports.usecase.validator;

import com.chaing.domain.transports.dto.OrderInfo;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.DeliverStatus;

import java.util.List;
import java.util.Map;

public interface TransportValidator {

    boolean canLoadWeight(Long maxLoad, Long currentTransitWeight);

    void checkLoadable(Long maxLoad, Long currentWeight, Long newWeight);

    void checkCancellable(DeliverStatus status);

    void checkTrackingNumber(List<OrderInfo> orders, Map<String, String> trackingMap);
}
