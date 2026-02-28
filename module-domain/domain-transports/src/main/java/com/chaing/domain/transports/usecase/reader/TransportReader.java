package com.chaing.domain.transports.usecase.reader;

import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.entity.Vehicle;

import java.util.List;

public interface TransportReader {

    List<Vehicle> findAvailableVehicles();

    Vehicle findVehicle(Long vehicleId);

    List<Transit> findTransits();

    Double calculateCurrentWeight(Long vehicleId);

}
