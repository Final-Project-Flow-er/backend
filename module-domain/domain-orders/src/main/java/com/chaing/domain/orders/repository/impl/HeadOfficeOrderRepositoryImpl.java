package com.chaing.domain.orders.repository.impl;

import com.chaing.domain.orders.dto.response.FactoryOrderItemProjection;
import com.chaing.domain.orders.dto.response.HQOrderItemProjection;
import com.chaing.domain.orders.entity.QHeadOfficeOrder;
import com.chaing.domain.orders.entity.QHeadOfficeOrderItem;
import com.chaing.domain.orders.enums.HQOrderStatus;
import com.chaing.domain.orders.repository.interfaces.HeadOfficeOrderRepositoryCustom;
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
public class HeadOfficeOrderRepositoryImpl implements HeadOfficeOrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<HQOrderItemProjection> findOrderItemPage(Pageable pageable) {
        QHeadOfficeOrder order = QHeadOfficeOrder.headOfficeOrder;

        List<HQOrderItemProjection> content = queryFactory
                .select(Projections.constructor(HQOrderItemProjection.class,
                        order.orderCode,
                        order.orderStatus,
                        order.userId,
                        order.totalQuantity,
                        order.totalAmount,
                        order.createdAt))
                .from(order)
                .where(order.deletedAt.isNull())
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(order.count())
                .from(order)
                .where(order.deletedAt.isNull())
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public Page<FactoryOrderItemProjection> findFactoryOrderItemPage(boolean isAll, Pageable pageable) {
        QHeadOfficeOrder order = QHeadOfficeOrder.headOfficeOrder;
        QHeadOfficeOrderItem item = QHeadOfficeOrderItem.headOfficeOrderItem;

        BooleanBuilder where = new BooleanBuilder();
        where.and(order.deletedAt.isNull());
        if (!isAll) {
            where.and(order.orderStatus.eq(HQOrderStatus.PENDING));
        }

        List<FactoryOrderItemProjection> content = queryFactory
                .select(Projections.constructor(FactoryOrderItemProjection.class,
                        order.orderCode,
                        order.orderStatus,
                        order.isRegular,
                        order.userId,
                        item.productId,
                        item.quantity,
                        order.createdAt,
                        order.storedDate))
                .from(item)
                .join(item.headOfficeOrder, order)
                .where(where)
                .orderBy(order.createdAt.desc(), item.headOfficeOrderItemId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(item.count())
                .from(item)
                .join(item.headOfficeOrder, order)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }
}
