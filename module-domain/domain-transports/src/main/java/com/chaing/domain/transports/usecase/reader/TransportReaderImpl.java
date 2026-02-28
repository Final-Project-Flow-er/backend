package com.chaing.domain.transports.usecase.reader;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.enums.Dispatchable;
import com.chaing.domain.transports.repository.TransitRepository;
import com.chaing.domain.transports.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransportReaderImpl implements TransportReader{

    private final VehicleRepository vehicleRepository;
    private final TransitRepository transitRepository;

    @Override
    public List<Vehicle> findAvailableVehicles() {
        return vehicleRepository.findAllByDispatchableAndStatus(Dispatchable.AVAILABLE, UsableStatus.ACTIVE);
    }

    @Override
    public Double calculateCurrentWeight(Long vehicleId) {

        List<Transit> activeTransits = transitRepository.findAllByVehicleIdAndStatus(vehicleId, DeliverStatus.IN_TRANSIT);

        return activeTransits.stream()
                .mapToDouble(Transit::getCargoWeight)
                .sum();
    }

    @Override
    public Vehicle findVehicle(Long vehicleId) {
        return null;
    }

    @Override
    public List<Transit> findTransits() {
        return List.of();
    }
}
