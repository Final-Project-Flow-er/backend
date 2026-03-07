package com.chaing.domain.inventories.repository.impl;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.response.ExpirationBatchResultResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.InventoryProductInfoResponse;
import com.chaing.domain.inventories.entity.QFranchiseInventory;
import com.chaing.domain.inventories.entity.QInventoryPolicy;
import com.chaing.domain.inventories.enums.LocationType;
import com.chaing.domain.inventories.exception.InventoryErrorCode;
import com.chaing.domain.inventories.exception.InventoryException;
import com.chaing.domain.inventories.repository.interfaces.FranchiseInventoryRepositoryCustom;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
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

@Repository
@RequiredArgsConstructor
public class FranchiseInventoryRepositoryImpl implements FranchiseInventoryRepositoryCustom {
        private final QInventoryPolicy inventoryPolicy = QInventoryPolicy.inventoryPolicy;
        private final QFranchiseInventory franchiseInventory = QFranchiseInventory.franchiseInventory;
        private final JPAQueryFactory queryFactory;
        // EntityManager 주입
        @PersistenceContext
        private EntityManager em;

        // 대분류
        @Override
        public Map<Long, InventoryProductInfoResponse> getFranchiseStock(Long franchiseId, List<Long> ids,
                        String status) {
                NumberExpression<Integer> quantity = franchiseInventory.inventoryId.count().intValue();

                StringExpression safetyResult = new CaseBuilder()
                                .when(quantity.gt(inventoryPolicy.safetyStock.add(20))).then("SAFE")
                                .when(quantity.gt(inventoryPolicy.safetyStock)).then("WARNING")
                                .otherwise("DANGER");

                List<InventoryProductInfoResponse> result = queryFactory
                                .select(Projections.constructor(
                                                InventoryProductInfoResponse.class,
                                                franchiseInventory.productId,
                                                quantity,
                                                inventoryPolicy.safetyStock,
                                                safetyResult))
                                .from(franchiseInventory)
                                .join(inventoryPolicy)
                                .on(
                                                franchiseInventory.productId.eq(inventoryPolicy.productId)
                                                                .and(inventoryPolicy.locationType
                                                                                .eq(LocationType.FRANCHISE)
                                                                                .and(inventoryPolicy.locationId
                                                                                                .eq(franchiseId))))
                                .where(
                                                franchiseInventory.franchiseId.eq(franchiseId),
                                                franchiseInventory.productId.in(ids))
                                .groupBy(franchiseInventory.productId, inventoryPolicy.safetyStock)
                                .having(status == null ? null : safetyResult.eq(status))
                                .fetch();

                return result.stream()
                                .collect(Collectors.toMap(
                                                InventoryProductInfoResponse::productId,
                                                r -> r));
        }

        // 중분류
        @Override
        public List<FranchiseInventoryBatchResponse> getFranchiseBatches(Long franchiseId, Long productId) {
                NumberExpression<Integer> quantity = franchiseInventory.inventoryId.count().intValue();

                NumberExpression<Integer> availableQuantity = new CaseBuilder()
                                .when(franchiseInventory.status.eq(LogType.AVAILABLE)).then(1)
                                .otherwise(0).sum().intValue();

                NumberExpression<Integer> returnPending = new CaseBuilder()
                                .when(franchiseInventory.status.eq(LogType.RETURN_WAIT)).then(1)
                                .otherwise(0).sum().intValue();

                return queryFactory
                                .select(Projections.constructor(
                                                FranchiseInventoryBatchResponse.class,
                                                franchiseInventory.manufactureDate,
                                                quantity,
                                                availableQuantity,
                                                returnPending))
                                .from(franchiseInventory)
                                .where(
                                                franchiseInventory.franchiseId.eq(franchiseId),
                                                franchiseInventory.productId.eq(productId))
                                .groupBy(franchiseInventory.manufactureDate)
                                .fetch();
        }

        // 소분류
        @Override
        public List<FranchiseInventoryItemResponse> getFranchiseItems(Long franchiseId,
                        FranchiseInventoryItemsRequest request) {
                return queryFactory
                                .select(Projections.constructor(
                                                FranchiseInventoryItemResponse.class,
                                                franchiseInventory.serialCode,
                                                franchiseInventory.boxCode,
                                                franchiseInventory.status.stringValue(),
                                                franchiseInventory.shippedAt,
                                                franchiseInventory.receivedAt))
                                .from(franchiseInventory)
                                .where(
                                                franchiseInventory.productId.eq(request.productId()),
                                                franchiseInventory.franchiseId.eq(franchiseId),
                                                containsSerialCode(request.serialCode()),
                                                franchiseInventory.manufactureDate.eq(request.manufactureDate()),
                                                containsShippedAt(request.shippedAt()),
                                                containsReceivedAt(request.receivedAt()))
                                .fetch();
        }

        @Override
        public void deleteFranchiseInventory(Long franchiseId, List<String> serialCode) {
                queryFactory
                                .delete(franchiseInventory)
                                .where(
                                                franchiseInventory.serialCode.in(serialCode),
                                                franchiseInventory.franchiseId.eq(franchiseId))
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
                                                franchiseInventory.franchiseId.eq(franchiseId))
                                .execute();

                em.flush();
                em.clear();
        }

    @Override
    public List<ExpirationBatchResultResponse> getExpirationAlerts(String locationType, Long locationId) {
        LocalDate today = LocalDate.now();
        // 유통기한 = 제조일자 + 1년
        // 0~5일 남은 제조일자의 범위 (오늘 - 1년 <= 제조일자 <= 오늘 + 5일 - 1년)
        LocalDate startManufactureDate = today.minusYears(1);
        LocalDate endManufactureDate = today.plusDays(5).minusYears(1);

        NumberExpression<Integer> quantity = franchiseInventory.count().intValue();

        List<Tuple> results = queryFactory
                .select(
                        franchiseInventory.productId,
                        franchiseInventory.manufactureDate,
                        quantity)
                .from(franchiseInventory)
                // QInventoryPolicy가 where절에서 사용되므로 join 필요
                .join(inventoryPolicy).on(franchiseInventory.productId.eq(inventoryPolicy.productId))
                .where(
                        containsLocationType(locationType),
                        containsLocationId(locationId),
                        franchiseInventory.manufactureDate.between(startManufactureDate,
                                endManufactureDate))
                .groupBy(franchiseInventory.productId, franchiseInventory.manufactureDate)
                .fetch();

        return results.stream().map(tuple -> {
            Long productId = tuple.get(franchiseInventory.productId);
            LocalDate manufactureDate = tuple.get(franchiseInventory.manufactureDate);
            Integer qty = tuple.get(quantity);

            // 자바 코드로 유통기한 및 남은 일수 계산
            LocalDate expirationDate = manufactureDate.plusYears(1);
            int daysUntilExpiration = (int) java.time.temporal.ChronoUnit.DAYS.between(today,
                    expirationDate);

            return new ExpirationBatchResultResponse(
                    productId,
                    manufactureDate,
                    qty,
                    daysUntilExpiration);
        }).collect(Collectors.toList());
    }

    private BooleanExpression containsSerialCode(String serialCode) {
                return serialCode != null ? franchiseInventory.serialCode.eq(serialCode) : null;
        }

        private BooleanExpression containsManufactureDate(LocalDate date) {
                return date != null ? franchiseInventory.manufactureDate.eq(date) : null;
        }

        private BooleanExpression containsShippedAt(LocalDate date) {
                if (date == null)
                        return null;

                LocalDateTime startOfDay = date.atStartOfDay(); // 2026-03-04T00:00
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay(); // 2026-03-05T00:00

                return franchiseInventory.shippedAt.goe(startOfDay) // >= 2026-03-04T00:00
                                .and(franchiseInventory.shippedAt.lt(endOfDay)); // < 2026-03-05T00:00
        }

        private BooleanExpression containsReceivedAt(LocalDate date) {
                if (date == null)
                        return null;

                LocalDateTime startOfDay = date.atStartOfDay(); // 2026-03-04T00:00
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay(); // 2026-03-05T00:00

                return franchiseInventory.receivedAt.goe(startOfDay) // >= 2026-03-04T00:00
                                .and(franchiseInventory.receivedAt.lt(endOfDay)); // < 2026-03-05T00:00
        }

    private BooleanExpression containsLocationType(String locationType) {
        if(locationType == null || locationType.isBlank()){
            throw new InventoryException(InventoryErrorCode.INVALID_LOCATION_TYPE);
        }
        try {
            LocationType type = LocationType.valueOf(locationType.toUpperCase());
            return QInventoryPolicy.inventoryPolicy.locationType.eq(type);
        } catch (IllegalArgumentException e) {
            throw new InventoryException(InventoryErrorCode.INVALID_LOCATION_TYPE);
        }
    }

    private BooleanExpression containsLocationId(Long locationId) {
        if(locationId == null){
            throw new InventoryException(InventoryErrorCode.INVALID_LOCATION_ID);
        }
        return QInventoryPolicy.inventoryPolicy.locationId.eq(locationId);
    }
}
