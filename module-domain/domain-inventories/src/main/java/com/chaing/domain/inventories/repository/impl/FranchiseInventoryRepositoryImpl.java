package com.chaing.domain.inventories.repository.impl;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.InventoryProductInfoResponse;
import com.chaing.domain.inventories.entity.QFranchiseInventory;
import com.chaing.domain.inventories.entity.QInventoryPolicy;
import com.chaing.domain.inventories.enums.LocationType;
import com.chaing.domain.inventories.repository.interfaces.FranchiseInventoryRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FranchiseInventoryRepositoryImpl implements FranchiseInventoryRepositoryCustom {
    private final QInventoryPolicy inventoryPolicy =  QInventoryPolicy.inventoryPolicy;
    private final QFranchiseInventory franchiseInventory =  QFranchiseInventory.franchiseInventory;
    private final JPAQueryFactory queryFactory;
    private EntityManager em;


    // 대분류
    @Override
    public Map<Long, InventoryProductInfoResponse> getFranchiseStock(Long franchiseId, List<Long> ids, String status) {
        NumberExpression<Integer> quantity =
                franchiseInventory.inventoryId.count().intValue();

        StringExpression safetyResult =
                new CaseBuilder()
                        .when(quantity.gt(inventoryPolicy.safetyStock.add(20))).then("SAFE")
                        .when(quantity.gt(inventoryPolicy.safetyStock)).then("WARNING")
                        .otherwise("DANGER");

        List<InventoryProductInfoResponse> result = queryFactory
                .select(Projections.constructor(
                        InventoryProductInfoResponse.class,
                        franchiseInventory.inventoryId,
                        quantity,
                        inventoryPolicy.safetyStock,
                        safetyResult
                ))
                .from(franchiseInventory)
                .join(inventoryPolicy)
                .on(
                        franchiseInventory.productId.eq(inventoryPolicy.productId)
                                .and(inventoryPolicy.locationType.eq(LocationType.FRANCHISE)
                                        .and(inventoryPolicy.id.eq(franchiseId)))
                )
                .where(
                        franchiseInventory.franchiseId.eq(franchiseId)
                )
                .groupBy(franchiseInventory.productId, inventoryPolicy.safetyStock)
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
    public List<FranchiseInventoryBatchResponse> getFranchiseBatches(Long franchiseId, Long productId) {
        NumberExpression<Integer> quantity =
                franchiseInventory.inventoryId.count().intValue();

        NumberExpression<Integer> availableQuantity =
                franchiseInventory.status.eq(LogType.AVAILABLE).count().intValue();

        NumberExpression<Integer> returnPending =
                franchiseInventory.status.eq(LogType.RETURN_WAIT).count().intValue();

        return queryFactory
                .select(Projections.constructor(
                        FranchiseInventoryBatchResponse.class,
                        franchiseInventory.manufactureDate,
                        quantity,
                        availableQuantity,
                        returnPending
                ))
                .from(franchiseInventory)
                .where(
                        franchiseInventory.franchiseId.eq(franchiseId)
                )
                .fetch();
    }

    // 소분류
    @Override
    public List<FranchiseInventoryItemResponse> getFranchiseItems(Long franchiseId, FranchiseInventoryItemsRequest request) {
        return queryFactory
                .select(Projections.constructor(
                        FranchiseInventoryItemResponse.class,
                        franchiseInventory.serialCode,
                        franchiseInventory.boxCode,
                        franchiseInventory.manufactureDate,
                        franchiseInventory.shippedAt,
                        franchiseInventory.receivedAt
                ))
                .from(franchiseInventory)
                .where(
                        franchiseInventory.productId.eq(request.productId()),
                        franchiseInventory.franchiseId.eq(franchiseId),
                        containsSerialCode(request.serialCode()),
                        franchiseInventory.manufactureDate.eq(request.manufactureDate()),
                        containsShippedAt(request.shippedAt()),
                        containsReceivedAt(request.receivedAt())
                )
                .fetch();
    }

    @Override
    public void deleteFranchiseInventory(Long franchiseId, List<String> serialCode) {
        queryFactory
                .delete(franchiseInventory)
                .where(
                        franchiseInventory.serialCode.in(serialCode),
                        franchiseInventory.franchiseId.eq(franchiseId)
                )
                .execute();
        em.flush();
        em.clear();
    }

    @Override
    public void updateFranchiseStatus(Long franchiseId, List<String> serialCode, LogType logType) {
        queryFactory
                .update(franchiseInventory)
                .set(franchiseInventory.status, logType)
                .where(
                        franchiseInventory.serialCode.in(serialCode),
                        franchiseInventory.franchiseId.eq(franchiseId)
                )
                .execute();

        em.flush();
        em.clear();
    }

    private BooleanExpression containsSerialCode(String serialCode) {
        return serialCode != null ? franchiseInventory.serialCode.eq(serialCode) : null;
    }

    private BooleanExpression containsManufactureDate(LocalDate date) {
        return date != null ? franchiseInventory.manufactureDate.eq(date) : null;
    }

    private BooleanExpression containsShippedAt(LocalDate date) {
        if (date == null) return null;

        LocalDateTime startOfDay = date.atStartOfDay();           // 2026-03-04T00:00
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay(); // 2026-03-05T00:00

        return franchiseInventory.shippedAt.goe(startOfDay)  // >= 2026-03-04T00:00
                .and(franchiseInventory.shippedAt.lt(endOfDay)); // < 2026-03-05T00:00
    }

    private BooleanExpression containsReceivedAt(LocalDate date) {
        if (date == null) return null;

        LocalDateTime startOfDay = date.atStartOfDay();           // 2026-03-04T00:00
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay(); // 2026-03-05T00:00

        return franchiseInventory.shippedAt.goe(startOfDay)  // >= 2026-03-04T00:00
                .and(franchiseInventory.shippedAt.lt(endOfDay)); // < 2026-03-05T00:00
    }
}
