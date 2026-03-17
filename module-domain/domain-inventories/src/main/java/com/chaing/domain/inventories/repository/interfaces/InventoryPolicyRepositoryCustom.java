package com.chaing.domain.inventories.repository.interfaces;

import com.chaing.domain.inventories.entity.InventoryPolicy;
import com.chaing.domain.inventories.enums.LocationType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryPolicyRepositoryCustom {
    Optional<InventoryPolicy> findPolicy(LocationType type, Long locationId, Long productId);

    long updateManualSafetyStock(LocationType type, Long locationId, Long productId, Integer safetyStock);
}
