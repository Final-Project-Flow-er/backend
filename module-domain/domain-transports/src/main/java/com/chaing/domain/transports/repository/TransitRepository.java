package com.chaing.domain.transports.repository;

import com.chaing.domain.transports.entity.Transit;
import com.chaing.domain.transports.enums.DeliverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TransitRepository extends JpaRepository<Transit, Long> {

    List<Transit> findByVehicleId(Long vehicleId);

    List<Transit> findByFranchiseId(Long franchiseId);
}
