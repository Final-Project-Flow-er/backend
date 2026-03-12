package com.chaing.domain.inventories.repository.impl;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.response.ExpirationBatchResultResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.InventoryProductInfoResponse;
import com.chaing.domain.inventories.dto.response.SafetyStockResponse;
import com.chaing.domain.inventories.entity.QFactoryInventory;
import com.chaing.domain.inventories.entity.QInventoryPolicy;
import com.chaing.domain.inventories.enums.LocationType;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.repository.interfaces.FactoryInventoryRepositoryCustom;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

                NumberExpression<Integer> quantity = factoryInventory.inventoryId.count().intValue();

                NumberExpression<Integer> effectiveSafetyStock = inventoryPolicy.safetyStock
                                .coalesce(inventoryPolicy.defaultSafetyStock);
                StringExpression safetyResult = new CaseBuilder()
                                .when(quantity.gt(effectiveSafetyStock.add(20))).then("SAFE")
                                .when(quantity.gt(effectiveSafetyStock)).then("WARNING")
                                .otherwise("DANGER");

                List<InventoryProductInfoResponse> result = queryFactory
                                .select(Projections.constructor(
                                                InventoryProductInfoResponse.class,
                                                factoryInventory.productId,
                                                quantity,
                                                effectiveSafetyStock,
                                                safetyResult))
                                .from(factoryInventory)
                                .leftJoin(inventoryPolicy)
                                .on(
                                                factoryInventory.productId.eq(inventoryPolicy.productId)
                                                                .and(inventoryPolicy.locationType
                                                                                .eq(LocationType.FACTORY))
                                                                .and(inventoryPolicy.locationId.eq(1L)))
                                .where(
                                                factoryInventory.productId.in(products),
                                                factoryInventory.status.eq(LogType.AVAILABLE))
                                .groupBy(factoryInventory.productId, inventoryPolicy.safetyStock,
                                                inventoryPolicy.defaultSafetyStock)
                                .having(status == null ? null : safetyResult.eq(status))
                                .fetch();

                return result.stream()
                                .collect(Collectors.toMap(
                                                InventoryProductInfoResponse::productId,
                                                r -> r));
        }

        // 중분류
        @Override
        public Page<HQInventoryBatchResponse> getBatches(Long productId, Pageable pageable) {
            NumberExpression<Integer> quantity = factoryInventory.inventoryId.count().intValue();

            List<HQInventoryBatchResponse> content = queryFactory
                    .select(Projections.constructor(
                            HQInventoryBatchResponse.class,
                            factoryInventory.manufactureDate,
                            quantity))
                    .from(factoryInventory)
                    .where(factoryInventory.productId.eq(productId))
                    .groupBy(factoryInventory.manufactureDate)
                    .orderBy(factoryInventory.manufactureDate.desc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();

            Long total = queryFactory
                    .select(factoryInventory.manufactureDate.countDistinct())
                    .from(factoryInventory)
                    .where(factoryInventory.productId.eq(productId))
                    .fetchOne();

            return new PageImpl<>(content, pageable, total == null ? 0L : total);
        }

    // 소분류
    @Override
    public Page<HQInventoryItemResponse> getItems(HQInventoryItemsRequest request, Pageable pageable) {
        List<HQInventoryItemResponse> content = queryFactory
                .select(Projections.constructor(
                        HQInventoryItemResponse.class,
                        factoryInventory.inventoryId,
                        factoryInventory.serialCode,
                        factoryInventory.boxCode,
                        factoryInventory.status.stringValue(),
                        factoryInventory.shippedAt,
                        factoryInventory.receivedAt))
                .from(factoryInventory)
                .where(
                        factoryInventory.productId.eq(request.productId()),
                        containsSerialCode(request.serialCode()),
                        containsManufactureDate(request.manufactureDate()),
                        containsShippedAt(request.shippedAt()),
                        containsReceivedAt(request.receivedAt()))
                .orderBy(factoryInventory.inventoryId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(factoryInventory.count())
                .from(factoryInventory)
                .where(
                        factoryInventory.productId.eq(request.productId()),
                        containsSerialCode(request.serialCode()),
                        containsManufactureDate(request.manufactureDate()),
                        containsShippedAt(request.shippedAt()),
                        containsReceivedAt(request.receivedAt()))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

        // 유통기한 체크
        @Override
        public List<ExpirationBatchResultResponse> getExpirationAlerts(String locationType, Long locationId) {
                if (!"FACTORY".equalsIgnoreCase(locationType)) {
                        throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_TYPE);
                }

                LocalDate today = LocalDate.now();
                // 유통기한 = 제조일자 + 1년
                // 0~5일 남은 제조일자의 범위 (오늘 - 1년 <= 제조일자 <= 오늘 + 5일 - 1년)
                LocalDate startManufactureDate = today.minusYears(1);
                LocalDate endManufactureDate = today.plusDays(5).minusYears(1);

                NumberExpression<Integer> quantity = factoryInventory.count().intValue();

                List<Tuple> results = queryFactory
                                .select(
                                                factoryInventory.productId,
                                                factoryInventory.manufactureDate,
                                                quantity)
                                .from(factoryInventory)
                                .where(
                                                factoryInventory.manufactureDate.between(startManufactureDate,
                                                                endManufactureDate))
                                .groupBy(factoryInventory.productId, factoryInventory.manufactureDate)
                                .fetch();

                return results.stream().map(tuple -> {
                        Long productId = tuple.get(factoryInventory.productId);
                        LocalDate manufactureDate = tuple.get(factoryInventory.manufactureDate);
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

        @Override
        public long updateExpiredStatus(LocalDate expirationDate) {
                long updatedCount = queryFactory
                                .update(factoryInventory)
                                .set(factoryInventory.status, LogType.EXPIRED)
                                .where(
                                                factoryInventory.manufactureDate.loe(expirationDate),
                                                factoryInventory.status.eq(LogType.AVAILABLE))
                                .execute();

                em.flush();
                em.clear();

                return updatedCount;
        }

        @Override
        public List<SafetyStockResponse> getLowStockAlerts(String locationType, Long locationId) {

                BooleanExpression typeFilter = containsLocationType(locationType);
                BooleanExpression idFilter = containsLocationId(locationId);

                NumberExpression<Integer> quantity = factoryInventory.inventoryId.count().intValue();
                NumberExpression<Integer> effectiveSafetyStock = inventoryPolicy.safetyStock
                                .coalesce(inventoryPolicy.defaultSafetyStock);

                return queryFactory
                                .select(Projections.constructor(
                                                SafetyStockResponse.class,
                                                inventoryPolicy.productId,
                                                quantity,
                                                effectiveSafetyStock))
                                .from(inventoryPolicy)
                                .leftJoin(factoryInventory)
                                .on(factoryInventory.productId.eq(inventoryPolicy.productId)
                                                .and(factoryInventory.status.eq(LogType.AVAILABLE)))
                                .where(
                                                typeFilter,
                                                idFilter)
                                .groupBy(inventoryPolicy.productId, inventoryPolicy.safetyStock,
                                                inventoryPolicy.defaultSafetyStock)
                                .having(quantity.loe(effectiveSafetyStock))
                                .fetch();
        }

        private BooleanExpression containsSerialCode(String serialCode) {
                return serialCode != null ? factoryInventory.serialCode.eq(serialCode) : null;
        }

        private BooleanExpression containsManufactureDate(LocalDate date) {
                return date != null ? factoryInventory.manufactureDate.eq(date) : null;
        }

        private BooleanExpression containsShippedAt(LocalDate date) {
                if (date == null)
                        return null;

                LocalDateTime startOfDay = date.atStartOfDay(); // 2026-03-04T00:00
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay(); // 2026-03-05T00:00

                return factoryInventory.shippedAt.goe(startOfDay) // >= 2026-03-04T00:00
                                .and(factoryInventory.shippedAt.lt(endOfDay)); // < 2026-03-05T00:00
        }

        private BooleanExpression containsReceivedAt(LocalDate date) {
                if (date == null)
                        return null;

                LocalDateTime startOfDay = date.atStartOfDay(); // 2026-03-04T00:00
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay(); // 2026-03-05T00:00

                return factoryInventory.receivedAt.goe(startOfDay) // >= 2026-03-04T00:00
                                .and(factoryInventory.receivedAt.lt(endOfDay)); // < 2026-03-05T00:00
        }

        private BooleanExpression containsLocationType(String locationType) {
                if (locationType == null || locationType.isBlank()) {
                        return null;
                }
                try {
                        LocationType type = LocationType.valueOf(locationType.toUpperCase());
                        return inventoryPolicy.locationType.eq(type);
                } catch (IllegalArgumentException e) {
                        throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_TYPE);
                }
        }

        private BooleanExpression containsLocationId(Long locationId) {
                if (locationId == null) {
                        return null;
                }
                return inventoryPolicy.locationId.eq(locationId);
        }
}
