package com.chaing.domain.transports.usecase.reader;

import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.entity.Vehicle;

import java.util.List;

public interface TransportReader {

    List<Vehicle> findCandidateVehicles();

    Long getCurrentTransitWeight(Long vehicleId);
}
