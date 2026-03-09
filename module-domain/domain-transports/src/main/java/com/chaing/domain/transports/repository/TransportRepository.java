package com.chaing.domain.transports.repository;

import com.chaing.domain.transports.entity.Transport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportRepository extends JpaRepository<Transport,Long> {

    Long findUnitPriceByTransportId(Long transportIdByVehicleId);
}
