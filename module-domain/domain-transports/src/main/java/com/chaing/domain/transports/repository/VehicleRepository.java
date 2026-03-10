package com.chaing.domain.transports.repository;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.Dispatchable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findAllByStatusAndDispatchable(UsableStatus status, Dispatchable dispatchable);

    @Query("SELECT v.maxLoad FROM Vehicle v WHERE v.vehicleId = :vehicleId")
    Long findMaxLoad(@Param("vehicleId") Long vehicleId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Vehicle v SET v.status = :status, v.dispatchable = (CASE " +
            "WHEN :status = com.chaing.core.enums.UsableStatus.ACTIVE " +
            "THEN com.chaing.domain.transports.enums.Dispatchable.AVAILABLE " +
            "ELSE com.chaing.domain.transports.enums.Dispatchable.UNAVAILABLE END) " +
            "WHERE v.transportId = :transportId AND v.deletedAt IS NULL")
    void updateStatusByTransportId(@Param("transportId") Long transportId, @Param("status") UsableStatus status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Vehicle v SET v.deletedAt = CURRENT_TIMESTAMP, " +
            "v.status = com.chaing.core.enums.UsableStatus.INACTIVE, " +
            "v.dispatchable = com.chaing.domain.transports.enums.Dispatchable.UNAVAILABLE " +
            "WHERE v.transportId = :transportId AND v.deletedAt IS NULL")
    void deleteVehiclesByTransportId(@Param("transportId") Long transportId);
}
