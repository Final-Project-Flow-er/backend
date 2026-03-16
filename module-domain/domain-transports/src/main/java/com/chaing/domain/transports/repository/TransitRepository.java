package com.chaing.domain.transports.repository;

import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.enums.DeliverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TransitRepository extends JpaRepository<Transit, Long> {

    List<Transit> findByVehicleId(Long vehicleId);

    @Query("select t from Transit t where t.orderCode in(:orderCodes)")
    List<Transit> findByOrderCodeIn(@Param("orderCodes") List<String> orderCodes);

    @Modifying(clearAutomatically = true)
    @Query("update Transit t set t.status = :targetStatus where t.orderCode = :orderCode")
    void updateDeliverStatus(@Param("orderCode") String orderCode, @Param("targetStatus") DeliverStatus targetStatus);
}
