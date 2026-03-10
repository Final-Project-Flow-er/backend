package com.chaing.domain.inventorylogs.repository.impl;

import com.chaing.core.enums.LogType;
import org.springframework.data.domain.Pageable;
import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.request.LogRequest;
import com.chaing.domain.inventorylogs.dto.response.BoxCodeResponse;
import com.chaing.domain.inventorylogs.dto.response.DailySales;
import com.chaing.domain.inventorylogs.dto.response.FactoryInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.FactoryInventoryLogResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogResponse;
import com.chaing.domain.inventorylogs.dto.response.ActorProductSalesResponse;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogResponse;
import com.chaing.domain.inventorylogs.dto.response.ProductSalesResponse;
import com.chaing.domain.inventorylogs.entity.QInventoryLog;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.exception.InventoryLogException;
import com.chaing.domain.inventorylogs.exception.InventoryLogtErrorCode;
import com.chaing.domain.inventorylogs.repository.interfaces.InventoryLogRepositoryCustom;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class InventoryLogRepositoryImpl implements InventoryLogRepositoryCustom {

        private final JPAQueryFactory queryFactory;
        private final QInventoryLog log = QInventoryLog.inventoryLog;

        @Override
        public InventoryLogListResponse findReturnInboundLogs(Long hqId, LogRequest request, Pageable pageable) {

                List<InventoryLogResponse> inventoryLogResponses = queryFactory
                                .select(Projections.constructor(
                                                InventoryLogResponse.class,
                                                log.createdAt.max(),
                                                log.transactionCode,
                                                Expressions.asString(""), // 박스 코드 - 펼쳤을때 조회하므로 빈값
                                                log.productName,
                                                log.logType,
                                                log.fromLocationId.max(),
                                                log.toLocationId.max(),
                                                log.quantity.sum().intValue()))
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.RETURN_INBOUND),
                                                locationContains("HQ", hqId), // 본사Id
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()))
                                .groupBy(
                                                log.transactionCode,
                                                log.productName,
                                                log.logType)
                                .orderBy(log.createdAt.max().desc(), log.transactionCode.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                long total = queryFactory
                                .select(log.transactionCode, log.productName, log.logType)
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.RETURN_INBOUND),
                                                locationContains("HQ", hqId), // 본사Id
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()))
                                .groupBy(
                                                log.transactionCode,
                                                log.productName,
                                                log.logType)
                                .fetch().size();

                return InventoryLogListResponse.builder()
                                .inventoryLogResponses(inventoryLogResponses)
                                .totalElements(total)
                                .totalPages((int) Math.ceil((double) total / pageable.getPageSize()))
                                .build();
        }

        @Override
        public InventoryLogListResponse findReturnOutboundLogs(Long hqId, LogRequest request, Pageable pageable) {
                List<InventoryLogResponse> inventoryLogResponses = queryFactory
                                .select(Projections.constructor(
                                                InventoryLogResponse.class,
                                                log.createdAt.max(),
                                                log.transactionCode,
                                                Expressions.asString(""), // 박스 코드 - 펼쳤을때 조회하므로 빈값
                                                log.productName,
                                                log.logType,
                                                log.fromLocationId.max(),
                                                log.toLocationId.max(),
                                                log.quantity.sum().intValue()))
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.RETURN_OUTBOUND),
                                                locationContains("HQ", hqId), // 본사Id
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()))
                                .groupBy(
                                                log.transactionCode,
                                                log.productName,
                                                log.logType)
                                .orderBy(log.createdAt.max().desc(), log.transactionCode.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                long total = queryFactory
                                .select(log.transactionCode, log.productName, log.logType)
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.RETURN_OUTBOUND),
                                                locationContains("HQ", 1L), // 본사Id
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()))
                                .groupBy(
                                                log.transactionCode,
                                                log.productName,
                                                log.logType)
                                .fetch().size();

                return InventoryLogListResponse.builder()
                                .inventoryLogResponses(inventoryLogResponses)
                                .totalElements(total)
                                .totalPages((int) Math.ceil((double) total / pageable.getPageSize()))
                                .build();
        }

        @Override
        public InventoryLogListResponse findDisposalLogs(Long hqId, LogRequest request, Pageable pageable) {
                List<InventoryLogResponse> inventoryLogResponses = queryFactory
                                .select(Projections.constructor(
                                                InventoryLogResponse.class,
                                                log.createdAt.max(),
                                                log.transactionCode,
                                                Expressions.asString(""), // 박스 코드 - 펼쳤을때 조회하므로 빈값
                                                log.productName,
                                                log.logType,
                                                log.fromLocationId.max(),
                                                log.toLocationId.max(),
                                                log.quantity.sum().intValue()))
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.DISPOSAL),
                                                locationContains("HQ", hqId), // 본사Id
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()))
                                .groupBy(
                                                log.transactionCode,
                                                log.productName,
                                                log.logType)
                                .orderBy(log.createdAt.max().desc(), log.transactionCode.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                long total = queryFactory
                                .select(log.transactionCode, log.productName, log.logType)
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.DISPOSAL),
                                                locationContains("HQ", 1L), // 본사Id
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()))
                                .groupBy(
                                                log.transactionCode,
                                                log.productName,
                                                log.logType)
                                .fetch().size();

                return InventoryLogListResponse.builder()
                                .inventoryLogResponses(inventoryLogResponses)
                                .totalElements(total)
                                .totalPages((int) Math.ceil((double) total / pageable.getPageSize()))
                                .build();
        }

        @Override
        public FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId,
                        FranchiseLogRequest request, Pageable pageable) {
                List<FranchiseInventoryLogResponse> franchiseInventoryLogResponseList = queryFactory
                                .select(Projections.constructor(
                                                FranchiseInventoryLogResponse.class,
                                                log.createdAt.max(),
                                                log.transactionCode,
                                                log.productName,
                                                log.logType,
                                                log.boxCode.countDistinct().intValue(),
                                                log.quantity.sum().intValue()))
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.INBOUND).or(log.logType.eq(LogType.OUTBOUND)),
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()),
                                                locationContains("FRANCHISE", franchiseId),
                                                containsProductName(request.productName()))
                                .groupBy(
                                                log.transactionCode,
                                                log.productName,
                                                log.logType)
                                .orderBy(log.createdAt.max().desc(), log.transactionCode.desc(), log.productName.desc(),
                                                log.logType.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                long total = queryFactory
                                .select(log.transactionCode, log.productName, log.logType)
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.INBOUND).or(log.logType.eq(LogType.OUTBOUND)),
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()),
                                                locationContains("FRANCHISE", franchiseId),
                                                containsProductName(request.productName()))
                                .groupBy(
                                                log.transactionCode,
                                                log.productName,
                                                log.logType)
                                .fetch().size();

                return FranchiseInventoryLogListResponse.builder()
                                .franchiseInventoryLogResponseList(franchiseInventoryLogResponseList)
                                .totalElements(total)
                                .totalPages((int) Math.ceil((double) total / pageable.getPageSize()))
                                .build();
        }

        @Override
        public List<BoxCodeResponse> findBoxCodesByTransactionCode(String transactionCode) {
                return queryFactory
                                .select(Projections.constructor(
                                                BoxCodeResponse.class,
                                                log.boxCode))
                                .from(log)
                                .where(log.transactionCode.eq(transactionCode))
                                .distinct()
                                .fetch();
        }

        @Override
        public FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId,
                        FranchiseLogRequest request, Pageable pageable) {
                List<FranchiseInventoryLogResponse> franchiseInventoryLogResponseList = queryFactory
                                .select(Projections.constructor(
                                                FranchiseInventoryLogResponse.class,
                                                log.createdAt,
                                                log.transactionCode,
                                                log.productName,
                                                log.logType,
                                                Expressions.asNumber(0),
                                                log.quantity))
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.SALE)
                                                                .or(log.logType.eq(LogType.REFUND)),
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()),
                                                locationContains("FRANCHISE", franchiseId),
                                                containsProductName(request.productName()))
                                .orderBy(log.createdAt.desc(), log.transactionCode.desc(), log.boxCode.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                long total = queryFactory
                                .select(log.count())
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.SALE).or(log.logType.eq(LogType.REFUND)),
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()),
                                                locationContains("FRANCHISE", franchiseId),
                                                containsProductName(request.productName()))
                                .fetchOne() == null ? 0L
                                                : queryFactory
                                                                .select(log.count())
                                                                .from(log)
                                                                .where(
                                                                                log.logType.eq(LogType.SALE).or(
                                                                                                log.logType.eq(LogType.REFUND)),
                                                                                betweenDate(request.startDate(),
                                                                                                request.endDate()),
                                                                                containsTransactionCode(request
                                                                                                .transactionCode()),
                                                                                locationContains("FRANCHISE",
                                                                                                franchiseId),
                                                                                containsProductName(
                                                                                                request.productName()))
                                                                .fetchOne();

                return FranchiseInventoryLogListResponse.builder()
                                .franchiseInventoryLogResponseList(franchiseInventoryLogResponseList)
                                .totalElements(total)
                                .totalPages((int) Math.ceil((double) total / pageable.getPageSize()))
                                .build();
        }

        @Override
        public FactoryInventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request,
                        Pageable pageable) {
                List<FactoryInventoryLogResponse> inventoryLogResponses = queryFactory
                                .select(Projections.constructor(
                                                FactoryInventoryLogResponse.class,
                                                log.createdAt.max(),
                                                log.transactionCode,
                                                log.productName,
                                                log.logType,
                                                log.boxCode.countDistinct().intValue(),
                                                log.quantity.sum().intValue()))
                                .from(log)
                                .where(
                                                locationContains("FACTORY", factoryId),
                                                containsProductName(request.productName()),
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()),
                                                containsLogType(request.logType()))
                                .groupBy(
                                                log.transactionCode,
                                                log.productName,
                                                log.logType)
                                .orderBy(log.createdAt.max().desc(), log.transactionCode.desc(), log.productName.desc(),
                                                log.logType.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                long total = queryFactory
                                .select(log.transactionCode, log.productName, log.logType)
                                .from(log)
                                .where(
                                                locationContains("FACTORY", factoryId),
                                                containsProductName(request.productName()),
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()),
                                                containsLogType(request.logType()))
                                .groupBy(
                                                log.transactionCode,
                                                log.productName,
                                                log.logType)
                                .fetch().size();

                return FactoryInventoryLogListResponse.builder()
                                .factoryInventoryLogResponseList(inventoryLogResponses)
                                .totalElements(total)
                                .totalPages((int) Math.ceil((double) total / pageable.getPageSize()))
                                .build();
        }

        // 날짜 조건문
        private BooleanExpression betweenDate(LocalDate startDate, LocalDate endDate) {
                LocalDateTime start = (startDate != null)
                                ? startDate.atStartOfDay()
                                : null;

                LocalDateTime end = (endDate != null)
                                ? endDate.atTime(LocalTime.MAX)
                                : null;

                if (start != null && end != null) {
                        return log.createdAt.between(start, end);
                }
                if (start != null) {
                        return log.createdAt.goe(start);
                }
                if (end != null) {
                        return log.createdAt.loe(end);
                }
                return null;
        }

        // 제품식별코드 조건문
        private BooleanExpression containsTransactionCode(String serialCode) {
                return serialCode != null ? QInventoryLog.inventoryLog.transactionCode.contains(serialCode) : null;
        }

        // 상품 이름 조건문
        private BooleanExpression containsProductName(String productName) {
                return productName != null ? QInventoryLog.inventoryLog.productName.contains(productName) : null;
        }

        // 해당 위치 로그 조회 조건문
        private BooleanExpression locationContains(String locationTypeStr, Long locationId) {
                com.chaing.domain.inventorylogs.enums.LocationType type;
                if (locationTypeStr == null || locationTypeStr.isBlank()) {
                        return null;
                }
                try {
                        type = com.chaing.domain.inventorylogs.enums.LocationType
                                        .valueOf(locationTypeStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                        return null;
                }

                BooleanExpression fromExp = log.fromLocationType.eq(type);
                if (locationId != null) {
                        fromExp = fromExp.and(log.fromLocationId.eq(locationId).or(log.fromLocationId.isNull()));
                }

                BooleanExpression toExp = log.toLocationType.eq(type);
                if (locationId != null) {
                        toExp = toExp.and(log.toLocationId.eq(locationId).or(log.toLocationId.isNull()));
                }

                return fromExp.or(toExp);
        }

        // Enum값 변환
        private ActorType parseActorType(String actorType) {
                if (actorType == null || actorType.isBlank()) {
                        return null;
                }
                try {
                        return ActorType.valueOf(actorType.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                        throw new InventoryLogException(InventoryLogtErrorCode.INVALID_ACTOR_TYPE);
                }
        }

        // 해당 로그만 조회
        private BooleanExpression containsLogType(String logType) {
                if (logType == null || logType.isBlank()) {
                        return null;
                }
                try {
                        LogType type = LogType.valueOf(logType.trim().toUpperCase());
                        return log.logType.eq(type);
                } catch (IllegalArgumentException e) {
                        throw new InventoryLogException(InventoryLogtErrorCode.INVALID_LOG_TYPE);
                }
        }

        // constructor 따로 빼기
        private Expression<InventoryLogResponse> inventoryLogProjection() {
                return Projections.constructor(
                                InventoryLogResponse.class,
                                log.createdAt,
                                log.transactionCode,
                                log.boxCode,
                                log.productName,
                                log.logType,
                                log.fromLocationId,
                                log.toLocationId,
                                log.quantity);
        }

        // 판매기록 가져오기
        public List<ActorProductSalesResponse> getProductSales(
                        List<Long> actorIds,
                        List<Long> productIds,
                        ActorType queryActorType,
                        LogType queryLogType) {

                LocalDate today = LocalDate.now();
                LocalDate startDate = today.minusDays(90);
                LocalDate endDate = today.minusDays(60);

                NumberExpression<Integer> quantity = log.quantity.sum().intValue();

                BooleanExpression actorIdsCondition;
                if (actorIds == null || actorIds.isEmpty()) {
                        actorIdsCondition = log.actorId.isNull();
                } else {
                        List<Long> nonNullActorIds = actorIds.stream()
                                        .filter(id -> id != null)
                                        .toList();

                        actorIdsCondition = null;
                        if (!nonNullActorIds.isEmpty()) {
                                actorIdsCondition = log.actorId.in(nonNullActorIds);
                        }

                        if (actorIds.contains(null)) {
                                actorIdsCondition = (actorIdsCondition == null)
                                                ? log.actorId.isNull()
                                                : actorIdsCondition.or(log.actorId.isNull());
                        }
                }

                DateExpression<LocalDate> datePath = Expressions.dateTemplate(LocalDate.class, "DATE({0})",
                                log.createdAt);

                List<Tuple> result = queryFactory
                                .select(
                                                log.actorId,
                                                log.productId,
                                                datePath,
                                                quantity)
                                .from(log)
                                .where(
                                                betweenDate(startDate, endDate),
                                                log.actorType.eq(queryActorType),
                                                log.logType.eq(queryLogType),
                                                actorIdsCondition,
                                                log.productId.in(productIds))
                                .groupBy(log.actorId, log.productId, datePath)
                                .fetch();

                Map<Long, Map<Long, List<DailySales>>> grouped = new HashMap<>();

                for (Tuple tuple : result) {

                        Long actorId = tuple.get(log.actorId);
                        Long productId = tuple.get(log.productId);
                        Object dateObj = tuple.get(datePath);
                        LocalDate date = (dateObj instanceof Date)
                                        ? ((Date) dateObj).toLocalDate()
                                        : (LocalDate) dateObj;
                        Integer qty = tuple.get(quantity);

                        grouped
                                        .computeIfAbsent(actorId != null ? actorId : -1L, k -> new HashMap<>())
                                        .computeIfAbsent(productId, k -> new ArrayList<>())
                                        .add(new DailySales(date, qty));
                }

                List<ActorProductSalesResponse> responses = new ArrayList<>();

                for (Map.Entry<Long, Map<Long, List<DailySales>>> actorEntry : grouped.entrySet()) {

                        Long actorId = actorEntry.getKey();
                        if (actorId == -1L)
                                actorId = null;
                        Map<Long, List<DailySales>> productMap = actorEntry.getValue();

                        List<ProductSalesResponse> productResponses = new ArrayList<>();

                        for (Map.Entry<Long, List<DailySales>> productEntry : productMap.entrySet()) {

                                Long productId = productEntry.getKey();
                                List<DailySales> sales = productEntry.getValue();

                                int totalSales = sales.stream()
                                                .mapToInt(DailySales::quantity)
                                                .sum();

                                productResponses.add(
                                                new ProductSalesResponse(productId, sales, totalSales));
                        }

                        responses.add(
                                        new ActorProductSalesResponse(actorId, productResponses));
                }

                return responses;
        }
}
