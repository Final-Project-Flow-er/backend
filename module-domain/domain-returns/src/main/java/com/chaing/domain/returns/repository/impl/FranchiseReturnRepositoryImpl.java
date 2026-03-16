package com.chaing.domain.returns.repository.impl;

import com.chaing.domain.returns.dto.response.FranchiseReturnItemProjection;
import com.chaing.domain.returns.dto.response.HQReturnItemProjection;
import com.chaing.domain.returns.entity.QReturnItem;
import com.chaing.domain.returns.entity.QReturns;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.repository.interfaces.FranchiseReturnRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FranchiseReturnRepositoryImpl implements FranchiseReturnRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FranchiseReturnItemProjection> findReturnItemPage(Long franchiseId, Pageable pageable) {
        QReturns returns = QReturns.returns;
        QReturnItem item = QReturnItem.returnItem;

        List<FranchiseReturnItemProjection> content = queryFactory
                .select(Projections.constructor(FranchiseReturnItemProjection.class,
                        returns.returnCode,
                        returns.returnStatus,
                        returns.franchiseOrderId,
                        item.franchiseOrderItemId,
                        returns.returnType,
                        returns.createdAt))
                .from(item)
                .join(item.returns, returns)
                .where(returns.franchiseId.eq(franchiseId),
                        returns.deletedAt.isNull())
                .orderBy(returns.createdAt.desc(), item.returnItemId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(item.count())
                .from(item)
                .join(item.returns, returns)
                .where(returns.franchiseId.eq(franchiseId),
                        returns.deletedAt.isNull())
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public Page<HQReturnItemProjection> findHQReturnPage(boolean isAll, Pageable pageable) {
        QReturns returns = QReturns.returns;

        BooleanBuilder where = new BooleanBuilder();
        where.and(returns.deletedAt.isNull());
        if (!isAll) {
            where.and(returns.returnStatus.eq(ReturnStatus.PENDING));
        }

        List<HQReturnItemProjection> content = queryFactory
                .select(Projections.constructor(HQReturnItemProjection.class,
                        returns.franchiseId,
                        returns.userId,
                        returns.createdAt,
                        returns.returnCode,
                        returns.returnStatus,
                        returns.returnType,
                        returns.totalReturnQuantity,
                        returns.totalReturnAmount))
                .from(returns)
                .where(where)
                .orderBy(returns.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(returns.count())
                .from(returns)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }
}
