package com.chaing.domain.transports.usecase.executor;

import com.chaing.domain.transports.dto.OrderInfo;
import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.repository.TransitRepository;
import com.chaing.domain.transports.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TransportExecutorImpl implements TransportExecutor {

    private final TransitRepository transitRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public void createTransits(Long vehicleId, List<OrderInfo> orders, Map<String, String> trackingMap, List<String> returnCodes) {

        List<Transit> transits = orders.stream()
                .flatMap(order -> returnCodes.stream()
                .map(code -> Transit.create(
                        vehicleId,
                        order.orderCode(),
                        order.weight(),
                        trackingMap.get(order.orderCode()),
                        order.franchiseId(),
                        code
                ))
                )
                .toList();

        transitRepository.saveAll(transits);
    }

    @Override
    public String cancelTransit(Long transportId) {
        Transit transit = transitRepository.findById(transportId)
                .orElseThrow(() -> new TransportException(TransportErrorCode.TRANSPORT_NOT_FOUND));

        String orderCode = transit.getOrderCode();

        transitRepository.delete(transit);

        return orderCode;
    }

    @Override
    public void updateDispatchableStatus(Long vehicleId) {
        vehicleRepository.updateDispatchable(vehicleId);
    }
}
