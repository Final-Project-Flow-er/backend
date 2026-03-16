package com.chaing.domain.orders.repository.impl;

import com.chaing.domain.orders.dto.response.FranchiseOrderItemProjection;
import com.chaing.domain.orders.dto.response.HQRequestedOrderItemProjection;
import com.chaing.domain.orders.entity.QFranchiseOrder;
import com.chaing.domain.orders.entity.QFranchiseOrderItem;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.repository.interfaces.FranchiseOrderRepositoryCustom;
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
public class FranchiseOrderRepositoryImpl implements FranchiseOrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FranchiseOrderItemProjection> findOrderItemPage(Long franchiseId, Long userId, Pageable pageable) {
        QFranchiseOrder order = QFranchiseOrder.franchiseOrder;
        QFranchiseOrderItem item = QFranchiseOrderItem.franchiseOrderItem;

        List<FranchiseOrderItemProjection> content = queryFactory
                .select(Projections.constructor(FranchiseOrderItemProjection.class,
                        order.orderCode,
                        order.orderStatus,
                        item.productId,
                        item.quantity,
                        item.unitPrice,
                        order.totalAmount,
                        order.createdAt,
                        order.deliveryDate))
                .from(item)
                .join(item.franchiseOrder, order)
                .where(order.franchiseId.eq(franchiseId),
                        order.userId.eq(userId),
                        order.deletedAt.isNull())
                .orderBy(order.createdAt.desc(), item.franchiseOrderItemId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(item.count())
                .from(item)
                .join(item.franchiseOrder, order)
                .where(order.franchiseId.eq(franchiseId),
                        order.userId.eq(userId),
                        order.deletedAt.isNull())
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public Page<HQRequestedOrderItemProjection> findRequestedOrderItemPage(boolean isPending, Pageable pageable) {
        QFranchiseOrder order = QFranchiseOrder.franchiseOrder;
        QFranchiseOrderItem item = QFranchiseOrderItem.franchiseOrderItem;

        BooleanBuilder where = new BooleanBuilder();
        where.and(order.deletedAt.isNull());
        if (isPending) {
            where.and(order.orderStatus.eq(FranchiseOrderStatus.PENDING));
        }

        List<HQRequestedOrderItemProjection> content = queryFactory
                .select(Projections.constructor(HQRequestedOrderItemProjection.class,
                        order.orderCode,
                        order.orderStatus,
                        order.userId,
                        item.productId,
                        item.quantity,
                        order.deliveryDate))
                .from(item)
                .join(item.franchiseOrder, order)
                .where(where)
                .orderBy(order.createdAt.desc(), item.franchiseOrderItemId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(item.count())
                .from(item)
                .join(item.franchiseOrder, order)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }
}
