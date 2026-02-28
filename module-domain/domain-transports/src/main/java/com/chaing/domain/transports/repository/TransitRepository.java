package com.chaing.domain.transports.repository;

import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.enums.DeliverStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransitRepository {
    List<Transit> findAllByVehicleIdAndStatus(Long vehicleId, DeliverStatus deliverStatus);
}
