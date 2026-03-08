package com.chaing.domain.inventories.repository;

import com.chaing.domain.inventories.entity.InventoryPolicy;
import com.chaing.domain.inventories.enums.LocationType;
import com.chaing.domain.inventories.repository.interfaces.InventoryPolicyRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryPolicyRepository extends JpaRepository<InventoryPolicy, Long>, InventoryPolicyRepositoryCustom {
    Optional<InventoryPolicy> findByLocationTypeAndLocationIdAndProductId(
            LocationType locationType,
            Long locationId,
            Long productId
    );
}
