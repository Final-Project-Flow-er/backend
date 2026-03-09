package com.chaing.domain.transports.usecase.reader;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.OrderInfo;
import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.enums.Dispatchable;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.repository.TransitRepository;
import com.chaing.domain.transports.repository.TransportRepository;
import com.chaing.domain.transports.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransportReaderImpl implements TransportReader {

    private final VehicleRepository vehicleRepository;
    private final TransitRepository transitRepository;
    private final TransportRepository transportRepository;

    @Override
    public List<Vehicle> findCandidateVehicles() {
        return vehicleRepository.findAllByStatusAndDispatchable(
                UsableStatus.ACTIVE, Dispatchable.AVAILABLE);
    }

    @Override
    public Long getCurrentTransitWeight(Long vehicleId) {
        return transitRepository.findByVehicleId(vehicleId)
                .stream()
                .filter(t -> t.getStatus() != DeliverStatus.DELIVERED)
                .mapToLong(t -> t.getWeight() != null ? t.getWeight() : 0L)
                .sum();
    }

    @Override
    public Long getVehicleMaxLoad(Long vehicleId) {
        return vehicleRepository.findMaxLoad(vehicleId);
    }

    @Override
    public DeliverStatus getTransitStatus(Long transportId) {
        return transitRepository.findById(transportId)
                .map(Transit::getStatus)
                .orElseThrow(() -> new TransportException(TransportErrorCode.TRANSPORT_NOT_FOUND));
    }

    @Override
    public Long getDeliveryFee(Long vehicleId) {
        return transportRepository
                .findUnitPriceByTransportId(vehicleRepository.findTransportIdByVehicleId(vehicleId));
    }
}