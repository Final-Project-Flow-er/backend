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
    @Query("UPDATE Vehicle v SET " +
            "v.updatedAt = CURRENT_TIMESTAMP, " +
            "v.status = com.chaing.core.enums.UsableStatus.INACTIVE, " +
            "v.dispatchable = com.chaing.domain.transports.enums.Dispatchable.UNAVAILABLE " +
            "WHERE v.transportId = :transportId AND v.deletedAt IS NULL")
    void deactivateVehiclesByTransportId(@Param("transportId") Long transportId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Vehicle v SET " +
            "v.deletedAt = CURRENT_TIMESTAMP, " +
            "v.updatedAt = CURRENT_TIMESTAMP, " +
            "v.status = com.chaing.core.enums.UsableStatus.INACTIVE, " +
            "v.dispatchable = com.chaing.domain.transports.enums.Dispatchable.UNAVAILABLE " +
            "WHERE v.transportId = :transportId AND v.deletedAt IS NULL")
    void deleteVehiclesByTransportId(@Param("transportId") Long transportId);

    @Query("SELECT v.transportId FROM Vehicle v WHERE v.vehicleId = :vehicleId")
    Long findTransportIdByVehicleId(Long vehicleId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Vehicle v SET v.dispatchable = 'DISPATCHED' where v.vehicleId = :vehicleId")
    void updateDispatchable(@Param("vehicleId") Long vehicleId);

    @Query("SELECT v FROM Vehicle v WHERE v.dispatchable in (:available, :dispatched)")
    List<Vehicle> findAllByDispatchable(@Param("available") Dispatchable available, @Param("dispatched") Dispatchable dispatched);
}
