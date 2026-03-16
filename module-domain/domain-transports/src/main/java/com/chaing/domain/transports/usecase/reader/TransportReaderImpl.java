package com.chaing.domain.transports.usecase.reader;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.OrderInfo;
import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.entity.TransportLog;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.enums.Dispatchable;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.repository.TransitRepository;
import com.chaing.domain.transports.repository.TransportLogRepository;
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
    private final TransportLogRepository transportLogRepository;

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
        Long transportId = vehicleRepository.findTransportIdByVehicleId(vehicleId);

        if(transportId == null) {
            throw new TransportException(TransportErrorCode.TRANSPORT_VENDOR_NOT_FOUND);
        }

        Long unitPrice = transportRepository.findUnitPriceByTransportId(transportId);

        if(unitPrice == null) {
            throw new TransportException(TransportErrorCode.TRANSPORT_PRICE_IS_NULL);
        }

        return unitPrice;
    }

    @Override
    public String getTransportName(Long transportId) {
        String transportName = transportRepository.findCompanyNameByTransportId(transportId);
        if(transportName == null) {
            throw new TransportException(TransportErrorCode.TRANSPORT_VENDOR_NOT_FOUND);
        }
        return transportName;
    }

    @Override
    public List<Vehicle> getAllAvailableVehicles() {
        return vehicleRepository.findAllByDispatchable(Dispatchable.DISPATCHED, Dispatchable.AVAILABLE);
    }

    @Override
    public List<Transit> getTransitInfo(List<String> orderCodes) {
        List<Transit> deliveryOrders = transitRepository.findByOrderCodeIn(orderCodes);

        if(deliveryOrders == null || deliveryOrders.isEmpty()) {
            throw new  TransportException(TransportErrorCode.TRANSPORT_NOT_FOUND);
        }

        long requestedCount = orderCodes.stream().distinct().count();
        long matchedCount = deliveryOrders.stream()
                .map(Transit::getOrderCode)
                .distinct()
                .count();

        if(requestedCount != matchedCount) {
            throw new  TransportException(TransportErrorCode.TRANSPORT_NOT_FOUND);
        }

        return deliveryOrders;
    }

    @Override
    public List<TransportLog> getTransportLogs() {
        return transportLogRepository.getAll();
    }

    @Override
    public List<Vehicle> getVehicles(List<Long> vehicleIds) {
        if (vehicleIds == null || vehicleIds.isEmpty()) {
            return List.of();
        }

        List<Long> uniqueVehicleIds = vehicleIds.stream().distinct().toList();
        List<Vehicle> vehicles = vehicleRepository.findAllById(uniqueVehicleIds);
        if (vehicles == null || vehicles.size() != uniqueVehicleIds.size()) {
            throw new TransportException(TransportErrorCode.TRANSPORT_VEHICLE_NOT_FOUND);
        }
        return vehicles;
    }
}