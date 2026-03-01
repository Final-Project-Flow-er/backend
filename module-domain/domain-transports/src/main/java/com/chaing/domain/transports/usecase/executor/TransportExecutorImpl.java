package com.chaing.domain.transports.usecase.executor;

import com.chaing.domain.transports.dto.OrderInfo;
import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.repository.TransitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TransportExecutorImpl implements TransportExecutor {

    private final TransitRepository transitRepository;

    @Override
    public void createTransits(Long vehicleId, List<OrderInfo> orders, Map<String, String> trackingMap) {

        List<Transit> transits = orders.stream()
                .map(order -> Transit.create(
                        vehicleId,
                        order.orderCode(),
                        order.weight(),
                        trackingMap.get(order.orderCode())
                ))
                .toList();

        transitRepository.saveAll(transits);
    }
}
