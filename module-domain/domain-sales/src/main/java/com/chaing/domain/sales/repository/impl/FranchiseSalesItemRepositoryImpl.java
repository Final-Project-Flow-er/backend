package com.chaing.domain.sales.repository.impl;

import com.chaing.domain.sales.dto.response.FranchiseSalesDailyQuantityResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import com.chaing.domain.sales.dto.response.QFranchiseSalesInfoResponse;
import com.chaing.domain.sales.entity.SalesItem;
import com.chaing.domain.sales.repository.interfaces.FranchiseSalesItemRepositoryCustom;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static com.chaing.domain.sales.entity.QSales.sales;
import static com.chaing.domain.sales.entity.QSalesItem.salesItem;

@Repository
@RequiredArgsConstructor
public class FranchiseSalesItemRepositoryImpl implements FranchiseSalesItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FranchiseSalesInfoResponse> searchAllSalesItems(Long franchiseId) {
        return queryFactory
                .select(new QFranchiseSalesInfoResponse(
                        sales.salesCode,
                        salesItem.createdAt,
                        salesItem.productCode,
                        salesItem.productName,
                        sales.quantity,
                        salesItem.unitPrice,
                        salesItem.unitPrice.multiply(sales.quantity),
                        sales.isCanceled
                ))
                .from(salesItem)
                .join(salesItem.sales, sales)
                .where(
                        sales.franchiseId.eq(franchiseId),
                        sales.isCanceled.eq(false)
                )
                .fetch();
    }

    @Override
    public Page<FranchiseSalesInfoResponse> searchAllSalesItemsPage(Long franchiseId, Pageable pageable) {
        List<FranchiseSalesInfoResponse> content = queryFactory
                .select(new QFranchiseSalesInfoResponse(
                        sales.salesCode,
                        salesItem.createdAt,
                        salesItem.productCode,
                        salesItem.productName,
                        sales.quantity,
                        salesItem.unitPrice,
                        salesItem.unitPrice.multiply(sales.quantity),
                        sales.isCanceled
                ))
                .from(salesItem)
                .join(salesItem.sales, sales)
                .where(
                        sales.franchiseId.eq(franchiseId),
                        sales.isCanceled.eq(false)
                )
                .orderBy(salesItem.createdAt.desc(), salesItem.salesItemId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(salesItem.count())
                .from(salesItem)
                .join(salesItem.sales, sales)
                .where(
                        sales.franchiseId.eq(franchiseId),
                        sales.isCanceled.eq(false)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public List<FranchiseSalesInfoResponse> searchAllCanceledSalesItems(Long franchiseId) {
        return queryFactory
                .select(new QFranchiseSalesInfoResponse(
                        sales.salesCode,
                        salesItem.createdAt,
                        salesItem.productCode,
                        salesItem.productName,
                        sales.quantity,
                        salesItem.unitPrice,
                        sales.totalAmount,
                        sales.isCanceled
                ))
                .from(salesItem)
                .join(salesItem.sales, sales)
                .where(
                        sales.franchiseId.eq(franchiseId),
                        sales.isCanceled.eq(true)
                )
                .fetch();
    }

    @Override
    public List<SalesItem> searchAllSalesItemsBySalesCode(Long franchiseId, String salesCode) {
        return queryFactory
                .selectFrom(salesItem)
                .join(salesItem.sales, sales)
                .where(
                        sales.franchiseId.eq(franchiseId),
                        sales.salesCode.eq(salesCode)
                )
                .fetch();
    }

    @Override
    public List<FranchiseSalesDailyQuantityResponse> searchDailyProductSalesForSafetyStock(
            List<Long> franchiseIds,
            List<Long> productIds,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (franchiseIds == null || franchiseIds.isEmpty()
                || productIds == null || productIds.isEmpty()
                || startDate == null || endDate == null) {
            return List.of();
        }

        DateExpression<java.sql.Date> salesDateExpr = Expressions.dateTemplate(
                java.sql.Date.class, "DATE({0})", sales.createdAt);

        var qtyExpr = sales.quantity.sum();

        List<com.querydsl.core.Tuple> rows = queryFactory
                .select(
                        sales.franchiseId,
                        salesItem.productId,
                        salesDateExpr,
                        qtyExpr
                )
                .from(salesItem)
                .join(salesItem.sales, sales)
                .where(
                        sales.isCanceled.isFalse(),
                        sales.franchiseId.in(franchiseIds),
                        salesItem.productId.in(productIds),
                        sales.createdAt.goe(startDate.atStartOfDay()),
                        sales.createdAt.lt(endDate.plusDays(1).atStartOfDay())
                )
                .groupBy(
                        sales.franchiseId,
                        salesItem.productId,
                        salesDateExpr
                )
                .fetch();

        return rows.stream()
                .map(row -> {
                    Date date = row.get(salesDateExpr);
                    Number qtyNumber = row.get(qtyExpr);

                    return new FranchiseSalesDailyQuantityResponse(
                            row.get(sales.franchiseId),
                            row.get(salesItem.productId),
                            date != null ? date.toLocalDate() : null,
                            qtyNumber != null ? qtyNumber.intValue() : 0
                    );
                })
                .toList();
    }
}