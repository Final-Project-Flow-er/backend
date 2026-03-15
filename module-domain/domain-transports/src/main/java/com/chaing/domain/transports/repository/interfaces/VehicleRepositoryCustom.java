package com.chaing.domain.transports.repository.interfaces;

import com.chaing.domain.transports.dto.condition.VehicleSearchCondition;
import com.chaing.domain.transports.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleRepositoryCustom {

    Page<Vehicle> searchVehicles(VehicleSearchCondition condition, Pageable pageable);
}
