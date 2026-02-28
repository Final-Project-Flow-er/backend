package com.chaing.domain.transports.usecase.validator;

import com.chaing.domain.transports.entity.Vehicle;

import java.util.List;

public interface TransportValidator {

    void validateListDispatchable(List<Vehicle> vehicles);
    void validateListUsableStatus(List<Vehicle> vehicles);
    void validateListWeightCapacity(List<Vehicle> vehicles, Double cargoWeight);

    void validateIsDispatchable(Vehicle vehicle);
    void validateIsUsable(Vehicle vehicle);
    void validateWeightCapacity(Vehicle vehicle, Double cargoWeight);}
