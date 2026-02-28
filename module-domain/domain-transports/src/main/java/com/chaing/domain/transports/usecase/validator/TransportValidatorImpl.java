package com.chaing.domain.transports.usecase.validator;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.Dispatchable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

@Component
public class TransportValidatorImpl implements TransportValidator {

    @Override
    public void validateListDispatchable(List<Vehicle> vehicles) {

        vehicles.removeIf(vehicle -> vehicle.getDispatchable() != Dispatchable.AVAILABLE);

        if (vehicles.isEmpty()) {
            throw new NoSuchElementException("배차 가능한 차량이 없습니다.");
        }
    }

    @Override
    public void validateListUsableStatus(List<Vehicle> vehicles) {

        vehicles.removeIf(vehicle -> vehicle.getStatus() != UsableStatus.ACTIVE);

        if (vehicles.isEmpty()) {
            throw new NoSuchElementException("배차 가능한 차량이 없습니다.");
        }
    }

    @Override
    public void validateListWeightCapacity(List<Vehicle> vehicles, Double cargoWeight) {

        vehicles.removeIf(vehicle -> vehicle.getMaxLoad() < cargoWeight);

        if (vehicles.isEmpty()) {
            throw new NoSuchElementException("배차 가능한 차량이 없습니다.");
        }
    }

    @Override
    public void validateIsDispatchable(Vehicle vehicle) {

    }

    @Override
    public void validateIsUsable(Vehicle vehicle) {

    }

    @Override
    public void validateWeightCapacity(Vehicle vehicle, Double cargoWeight) {

    }
}
