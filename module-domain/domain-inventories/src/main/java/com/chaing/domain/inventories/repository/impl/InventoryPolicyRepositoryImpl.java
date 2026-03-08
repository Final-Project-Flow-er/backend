package com.chaing.domain.inventories.repository.impl;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.response.SafetyStockResponse;
import com.chaing.domain.inventories.entity.QFactoryInventory;
import com.chaing.domain.inventories.entity.QFranchiseInventory;
import com.chaing.domain.inventories.entity.QInventoryPolicy;
import com.chaing.domain.inventories.enums.LocationType;
import com.chaing.domain.inventories.exception.InventoryErrorCode;
import com.chaing.domain.inventories.exception.InventoryException;
import com.chaing.domain.inventories.repository.interfaces.InventoryPolicyRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class InventoryPolicyRepositoryImpl implements InventoryPolicyRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QInventoryPolicy inventoryPolicy = QInventoryPolicy.inventoryPolicy;
    private final QFranchiseInventory franchiseInventory = QFranchiseInventory.franchiseInventory;
    private final QFactoryInventory  factoryInventory = QFactoryInventory.factoryInventory;

    @Override
    public List<SafetyStockResponse> getLowStockAlerts(String locationType, Long locationId) {
        NumberExpression<Integer> quantity = factoryInventory.inventoryId.count().intValue();

        return queryFactory
                .select(Projections.constructor(
                        SafetyStockResponse.class,
                        inventoryPolicy.productId,
                        quantity,
                        inventoryPolicy.safetyStock
                ))
                .from(inventoryPolicy)
                .join(factoryInventory)
                .on(
                        factoryInventory.productId.eq(inventoryPolicy.productId)
                )
                .where(
                        containsLocationType(locationType),
                        containsLocationId(locationId),
                        factoryInventory.status.eq(LogType.AVAILABLE)

                )
                .groupBy(inventoryPolicy.productId)
                .having(quantity.lt(inventoryPolicy.safetyStock))
                .fetch();
    }



    private BooleanExpression containsLocationType(String locationType) {
        try{
            LocationType type =  LocationType.valueOf(locationType.toUpperCase());
            return QInventoryPolicy.inventoryPolicy.locationType.eq(type);
        }catch (IllegalArgumentException e){
            throw new InventoryException(InventoryErrorCode.INVALID_LOCATION_TYPE);
        }
    }

    private BooleanExpression containsLocationId(Long locationId) {
        try{
            return QInventoryPolicy.inventoryPolicy.locationId.eq(locationId);
        }catch (IllegalArgumentException e){
            throw new InventoryException(InventoryErrorCode.INVALID_LOCATION_ID);
        }
    }

}
