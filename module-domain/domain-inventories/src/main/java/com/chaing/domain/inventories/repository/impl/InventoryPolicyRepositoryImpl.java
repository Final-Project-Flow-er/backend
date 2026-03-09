package com.chaing.domain.inventories.repository.impl;

import com.chaing.domain.inventories.entity.InventoryPolicy;
import com.chaing.domain.inventories.entity.QInventoryPolicy;
import com.chaing.domain.inventories.enums.LocationType;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.repository.interfaces.InventoryPolicyRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class InventoryPolicyRepositoryImpl implements InventoryPolicyRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QInventoryPolicy inventoryPolicy = QInventoryPolicy.inventoryPolicy;

    @Override
    public Optional<InventoryPolicy> findPolicy(LocationType type, Long locationId, Long productId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(inventoryPolicy)
                .where(
                        inventoryPolicy.locationType.eq(type),
                        containsLocationId(locationId),
                        inventoryPolicy.productId.eq(productId))
                .fetchOne());
    }

    @Override
    public long updateManualSafetyStock(LocationType type, Long locationId, Long productId, Integer safetyStock) {
        return queryFactory.update(inventoryPolicy)
                .set(inventoryPolicy.safetyStock, safetyStock)
                .where(
                        inventoryPolicy.locationType.eq(type),
                        containsLocationId(locationId),
                        inventoryPolicy.productId.eq(productId))
                .execute();
    }

    private BooleanExpression containsLocationType(String locationType) {
        try {
            LocationType type = LocationType.valueOf(locationType.toUpperCase());
            return QInventoryPolicy.inventoryPolicy.locationType.eq(type);
        } catch (IllegalArgumentException e) {
            throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_TYPE);
        }
    }

    private BooleanExpression containsLocationId(Long locationId) {
        if (locationId == null) {
            return QInventoryPolicy.inventoryPolicy.locationId.isNull();
        }
        return QInventoryPolicy.inventoryPolicy.locationId.eq(locationId);
    }

}
