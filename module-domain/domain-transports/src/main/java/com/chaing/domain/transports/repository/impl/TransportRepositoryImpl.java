package com.chaing.domain.transports.repository.impl;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.condition.TransportSearchCondition;
import com.chaing.domain.transports.entity.QTransport;
import com.chaing.domain.transports.entity.Transport;
import com.chaing.domain.transports.repository.interfaces.TransportRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class TransportRepositoryImpl implements TransportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Transport> searchTransports(TransportSearchCondition condition, Pageable pageable) {

        QTransport transport = QTransport.transport;

        List<Transport> content = queryFactory
                .selectFrom(transport)
                .where(
                        companyNameContains(condition.companyName()),
                        managerContains(condition.manager()),
                        unitPriceLoe(condition.unitPrice()),
                        regionEq(condition.usableRegion()),
                        statusEq(condition.status())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(transport.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(transport.count())
                .from(transport)
                .where(
                        companyNameContains(condition.companyName()),
                        managerContains(condition.manager()),
                        unitPriceLoe(condition.unitPrice()),
                        regionEq(condition.usableRegion()),
                        statusEq(condition.status())
                )
                .fetchOne();

        long totalCount = (total != null) ? total : 0L;
        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression companyNameContains(String companyName) {
        return hasText(companyName) ? QTransport.transport.companyName.contains(companyName) : null;
    }

    private BooleanExpression managerContains(String manager) {
        return hasText(manager) ? QTransport.transport.manager.contains(manager) : null;
    }

    private BooleanExpression unitPriceLoe(Long unitPrice) {
        return unitPrice != null ? QTransport.transport.unitPrice.loe(unitPrice) : null;
    }

    private BooleanExpression regionEq(Region region) {
        return region != null ? QTransport.transport.usableRegion.eq(region) : null;
    }

    private BooleanExpression statusEq(UsableStatus status) {
        return status != null ? QTransport.transport.status.eq(status) : null;
    }
}
