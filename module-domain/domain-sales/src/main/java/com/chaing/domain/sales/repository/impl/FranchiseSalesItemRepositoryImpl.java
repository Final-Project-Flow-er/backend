package com.chaing.domain.sales.repository.impl;

import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import com.chaing.domain.sales.dto.response.QFranchiseSalesInfoResponse;
import com.chaing.domain.sales.repository.interfaces.FranchiseSalesItemRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
                        salesItem.quantity,
                        salesItem.unitPrice,
                        sales.totalAmount
                ))
                .from(salesItem)
                .join(salesItem.sales, sales)
                .where(
                        sales.franchiseId.eq(franchiseId)
                )
                .fetch();
    }
}
