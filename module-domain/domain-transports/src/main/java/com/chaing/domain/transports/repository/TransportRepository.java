package com.chaing.domain.transports.repository;

import com.chaing.domain.transports.entity.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransportRepository extends JpaRepository<Transport,Long> {

    @Query("select t.unitPrice from Transport t where :transportId")
    Long findUnitPriceByTransportId(Long transportIdByVehicleId);
}
