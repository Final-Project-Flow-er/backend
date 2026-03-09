package com.chaing.domain.inventorylogs.repository.impl;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.request.LogRequest;
import com.chaing.domain.inventorylogs.dto.response.DailySales;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseProductSalesResponse;
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
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
        public InventoryLogListResponse findReturnInboundLogs(LogRequest request) {

                List<InventoryLogResponse> inventoryLogResponses = queryFactory
                                .select(inventoryLogProjection())
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.RETURN_INBOUND),
                                                actorContains("HQ", 1L), // 본사Id
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()))
                                .orderBy(log.createdAt.desc())
                                .fetch();

                return InventoryLogListResponse.builder()
                                .inventoryLogResponses(inventoryLogResponses)
                                .build();
        }

        @Override
        public InventoryLogListResponse findReturnOutboundLogs(LogRequest request) {
                List<InventoryLogResponse> inventoryLogResponses = queryFactory
                                .select(inventoryLogProjection())
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.RETURN_OUTBOUND),
                                                actorContains("HQ", 1L), // 본사Id
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()))
                                .orderBy(log.createdAt.desc())
                                .fetch();

                return InventoryLogListResponse.builder()
                                .inventoryLogResponses(inventoryLogResponses)
                                .build();
        }

        @Override
        public InventoryLogListResponse findDisposalLogs(LogRequest request) {
                List<InventoryLogResponse> inventoryLogResponses = queryFactory
                                .select(inventoryLogProjection())
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.DISPOSAL),
                                                actorContains("HQ", 1L), // 본사Id
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()))
                                .orderBy(log.createdAt.desc())
                                .fetch();

                return InventoryLogListResponse.builder()
                                .inventoryLogResponses(inventoryLogResponses)
                                .build();
        }

        @Override
        public FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId,
                        FranchiseLogRequest request) {
                List<FranchiseInventoryLogResponse> franchiseInventoryLogResponseList = queryFactory
                                .select(Projections.constructor(
                                                FranchiseInventoryLogResponse.class,
                                                log.createdAt,
                                                log.transactionCode,
                                                log.boxCode,
                                                log.productName,
                                                log.logType,
                                                log.supplyPrice,
                                                log.quantity))
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.INBOUND).or(log.logType.eq(LogType.OUTBOUND)),
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()),
                                                actorContains("FRANCHISE", franchiseId),
                                                containsProductName(request.productName()))
                                .orderBy(log.createdAt.desc())
                                .fetch();
                return FranchiseInventoryLogListResponse.builder()
                                .franchiseInventoryLogResponseList(franchiseInventoryLogResponseList)
                                .build();
        }

        @Override
        public FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId,
                        FranchiseLogRequest request) {
                List<FranchiseInventoryLogResponse> franchiseInventoryLogResponseList = queryFactory
                                .select(Projections.constructor(
                                                FranchiseInventoryLogResponse.class,
                                                log.createdAt,
                                                log.transactionCode,
                                                log.boxCode,
                                                log.productName,
                                                log.logType,
                                                log.quantity,
                                                log.price,
                                                log.quantity))
                                .from(log)
                                .where(
                                                log.logType.eq(LogType.SALE)
                                                                .or(log.logType.eq(LogType.REFUND)),
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()),
                                                actorContains("FRANCHISE", franchiseId),
                                                containsProductName(request.productName()))
                                .orderBy(log.createdAt.desc())
                                .fetch();
                return FranchiseInventoryLogListResponse.builder()
                                .franchiseInventoryLogResponseList(franchiseInventoryLogResponseList)
                                .build();
        }

        @Override
        public InventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request) {
                List<InventoryLogResponse> inventoryLogResponses = queryFactory
                                .select(inventoryLogProjection())
                                .from(log)
                                .where(
                                                actorContains("FACTORY", factoryId),
                                                containsProductName(request.productName()),
                                                betweenDate(request.startDate(), request.endDate()),
                                                containsTransactionCode(request.transactionCode()),
                                                containsLogType(request.logType()))
                                .orderBy(log.createdAt.desc())
                                .fetch();
                return InventoryLogListResponse.builder()
                                .inventoryLogResponses(inventoryLogResponses)
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

        // 해당 행위 로그 조회 조건문
        private BooleanExpression actorContains(String locationType, Long actorId) {

                ActorType type = parseActorType(locationType);

                if (type == null) {
                        return null;
                }

                return log.actorType.eq(type)
                                .and(log.actorId.eq(actorId));
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
                                log.supplyPrice,
                                log.quantity);
        }

        // 판매기록 가져오기
        @Override
        public List<FranchiseProductSalesResponse> getProductSales(
                        List<Long> actorIds,
                        List<Long> productIds,
                        ActorType queryActorType,
                        LogType queryLogType) {

                LocalDate today = LocalDate.now();
                LocalDate startDate = today.minusDays(90);
                LocalDate endDate = today.minusDays(60);

                NumberExpression<Integer> quantity = log.quantity.sum().intValue();

                List<Tuple> result = queryFactory
                                .select(
                                                log.actorId,
                                                log.productId,
                                                log.createdAt,
                                                quantity)
                                .from(log)
                                .where(
                                                betweenDate(startDate, endDate),
                                                log.actorType.eq(queryActorType),
                                                log.logType.eq(queryLogType),
                                                log.actorId.in(actorIds),
                                                log.productId.in(productIds))
                                .groupBy(log.actorId, log.productId, log.createdAt)
                                .fetch();

                Map<Long, Map<Long, List<DailySales>>> grouped = new HashMap<>();

                for (Tuple tuple : result) {

                        Long franchiseId = tuple.get(log.actorId);
                        Long productId = tuple.get(log.productId);
                        LocalDate date = tuple.get(log.createdAt).toLocalDate();
                        Integer qty = tuple.get(quantity);

                        grouped
                                        .computeIfAbsent(franchiseId, k -> new HashMap<>())
                                        .computeIfAbsent(productId, k -> new ArrayList<>())
                                        .add(new DailySales(date, qty));
                }

                List<FranchiseProductSalesResponse> responses = new ArrayList<>();

                for (Map.Entry<Long, Map<Long, List<DailySales>>> franchiseEntry : grouped.entrySet()) {

                        Long franchiseId = franchiseEntry.getKey();
                        Map<Long, List<DailySales>> productMap = franchiseEntry.getValue();

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
                                        new FranchiseProductSalesResponse(franchiseId, productResponses));
                }

                return responses;
        }
}
