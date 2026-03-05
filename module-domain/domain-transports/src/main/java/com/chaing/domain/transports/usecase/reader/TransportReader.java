package com.chaing.domain.transports.usecase.reader;

import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.DeliverStatus;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface TransportReader {

    List<Vehicle> findCandidateVehicles();

    Long getCurrentTransitWeight(Long vehicleId);

    Long getVehicleMaxLoad(@NotNull(message = "차량을 선택해주세요") Long vehicleId);

    DeliverStatus getTransitStatus(Long transportId);
}
