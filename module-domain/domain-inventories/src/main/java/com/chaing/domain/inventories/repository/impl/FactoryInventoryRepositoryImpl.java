package com.chaing.domain.inventories.repository.impl;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.response.ExpirationBatchResultResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.InventoryProductInfoResponse;
import com.chaing.domain.inventories.entity.QFactoryInventory;
import com.chaing.domain.inventories.entity.QInventoryPolicy;
import com.chaing.domain.inventories.enums.LocationType;
import com.chaing.domain.inventories.repository.interfaces.FactoryInventoryRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Repository
public class FactoryInventoryRepositoryImpl implements FactoryInventoryRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private final JPAQueryFactory queryFactory;
    private final QFactoryInventory factoryInventory = QFactoryInventory.factoryInventory;
    private final QInventoryPolicy inventoryPolicy = QInventoryPolicy.inventoryPolicy;

    // 대분류
    @Override
    public Map<Long, InventoryProductInfoResponse> getStock(List<Long> products, String status) {

        NumberExpression<Integer> quantity =
                factoryInventory.inventoryId.count().intValue();

        StringExpression safetyResult =
                new CaseBuilder()
                        .when(quantity.gt(inventoryPolicy.safetyStock.add(20))).then("SAFE")
                        .when(quantity.gt(inventoryPolicy.safetyStock)).then("WARNING")
                        .otherwise("DANGER");

        List<InventoryProductInfoResponse> result = queryFactory
                .select(Projections.constructor(
                        InventoryProductInfoResponse.class,
                        factoryInventory.inventoryId,
                        quantity,
                        inventoryPolicy.safetyStock,
                        safetyResult
                ))
                .from(factoryInventory)
                .join(inventoryPolicy)
                .on(
                        factoryInventory.productId.eq(inventoryPolicy.productId)
                                .and(inventoryPolicy.locationType.eq(LocationType.HQ))
                )
                .where(factoryInventory.productId.in(products))
                .groupBy(factoryInventory.productId, inventoryPolicy.safetyStock)
                .having(status == null ? null : safetyResult.eq(status))
                .fetch();

        return result.stream()
                .collect(Collectors.toMap(
                        InventoryProductInfoResponse::productId,
                        r -> r
                ));
    }

    // 중분류
    @Override
    public List<HQInventoryBatchResponse> getBatches(Long productId) {
        NumberExpression<Integer> quantity =
                factoryInventory.manufactureDate.count().intValue();

        return queryFactory
                .select(Projections.constructor(
                        HQInventoryBatchResponse.class,
                        factoryInventory.manufactureDate,
                        quantity
                ))
                .from(factoryInventory)
                .where(factoryInventory.productId.eq(productId))
                .groupBy(factoryInventory.manufactureDate)
                .fetch();
    }

    // 소분류
    @Override
    public List<HQInventoryItemResponse> getItems(HQInventoryItemsRequest request) {
        return queryFactory
                .select(Projections.constructor(
                        HQInventoryItemResponse.class,
                        factoryInventory.serialCode,
                        factoryInventory.boxCode,
                        factoryInventory.shippedAt,
                        factoryInventory.receivedAt
                ))
                .from(factoryInventory)
                .where(
                        factoryInventory.productId.eq(request.productId()),
                        containsSerialCode(request.serialCode()),
                        factoryInventory.manufactureDate.eq(request.manufactureDate()),
                        containsShippedAt(request.shippedAt()),
                        containsReceivedAt(request.receivedAt())
                )
                .fetch();
    }

    // 유통기한 체크
    @Override
    public List<ExpirationBatchResultResponse> getExpirationAlerts(String locationType, Long locationId) {

        NumberExpression<Integer> quantity = factoryInventory.count().intValue();

        NumberExpression<Integer> daysUntilExpiration =
                Expressions.numberTemplate(
                        Integer.class,
                        "DATEDIFF(DATE_ADD({0}, INTERVAL 1 YEAR), CURDATE())",
                        factoryInventory.manufactureDate
                );

        return queryFactory
                .select(Projections.constructor(
                        ExpirationBatchResultResponse.class,
                        factoryInventory.productId,
                        factoryInventory.manufactureDate,
                        quantity,
                        daysUntilExpiration
                ))
                .from(factoryInventory)
                .where(
                        containsLocationType(locationType),
                        containsLocationId(locationId),
                        daysUntilExpiration.loe(5)
                )
                .groupBy(factoryInventory.productId, factoryInventory.manufactureDate)
                .fetch();
    }

    @Override
    public void updateStatus(List<String> serialCode, LogType status) {

        queryFactory
                .update(factoryInventory)
                .set(factoryInventory.status, status)
                .where(factoryInventory.serialCode.in(serialCode))
                .execute();

        em.flush();
        em.clear();
    }

    @Override
    public void deleteFactoryInventory(List<String> serialCode) {
        queryFactory
                .delete(factoryInventory)
                .where(factoryInventory.serialCode.in(serialCode))
                .execute();

        em.flush();
        em.clear();
    }


    private BooleanExpression containsSerialCode(String serialCode) {
        return serialCode != null ? factoryInventory.serialCode.eq(serialCode) : null;
    }

    private BooleanExpression containsManufactureDate(LocalDate date) {
        return date != null ? factoryInventory.manufactureDate.eq(date) : null;
    }
    private BooleanExpression containsShippedAt(LocalDate date) {
        if (date == null) return null;

        LocalDateTime startOfDay = date.atStartOfDay();           // 2026-03-04T00:00
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay(); // 2026-03-05T00:00

        return factoryInventory.shippedAt.goe(startOfDay)  // >= 2026-03-04T00:00
                .and(factoryInventory.shippedAt.lt(endOfDay)); // < 2026-03-05T00:00
    }

    private BooleanExpression containsReceivedAt(LocalDate date) {
        if (date == null) return null;

        LocalDateTime startOfDay = date.atStartOfDay();           // 2026-03-04T00:00
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay(); // 2026-03-05T00:00

        return factoryInventory.shippedAt.goe(startOfDay)  // >= 2026-03-04T00:00
                .and(factoryInventory.shippedAt.lt(endOfDay)); // < 2026-03-05T00:00
    }

    private BooleanExpression containsLocationType(String locationType) {
        try{
            LocationType type =  LocationType.valueOf(locationType.toUpperCase());
            return QInventoryPolicy.inventoryPolicy.locationType.eq(type);
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("유효하지 않은 LocationType입니다: " + locationType);
        }
    }

    private BooleanExpression containsLocationId(Long locationId) {
        try{
            return QInventoryPolicy.inventoryPolicy.locationId.eq(locationId);
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("유효하지 않은 LocationType입니다: " + locationId);
        }
    }
}
