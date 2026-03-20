package com.chaing.domain.businessunits.repository.impl;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.condition.BusinessUnitSearchCondition;
import com.chaing.domain.businessunits.entity.Factory;
import com.chaing.domain.businessunits.entity.QFactory;
import com.chaing.domain.businessunits.repository.interfaces.FactoryRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class FactoryRepositoryImpl implements FactoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Factory> search(BusinessUnitSearchCondition condition, Pageable pageable) {

        QFactory factory = QFactory.factory;

        List<Factory> content = queryFactory
                .selectFrom(factory)
                .where(
                        nameOrCodeContains(condition.code(), condition.name()),
                        representativeOrBusinessNumberContains(condition.representativeName(), condition.businessNumber()),
                        regionEq(condition.region()),
                        statusEq(condition.status())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(factory.factoryId.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(factory.count())
                .from(factory)
                .where(
                        nameOrCodeContains(condition.code(), condition.name()),
                        representativeOrBusinessNumberContains(condition.representativeName(), condition.businessNumber()),
                        regionEq(condition.region()),
                        statusEq(condition.status())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameOrCodeContains(String code, String name) {
        BooleanExpression codeExpr = hasText(code) ? QFactory.factory.factoryCode.containsIgnoreCase(code) : null;
        BooleanExpression nameExpr = hasText(name) ? QFactory.factory.name.containsIgnoreCase(name) : null;
        if (codeExpr != null && nameExpr != null) return codeExpr.or(nameExpr);
        if (codeExpr != null) return codeExpr;
        return nameExpr;
    }

    private BooleanExpression representativeOrBusinessNumberContains(String representativeName, String businessNumber) {
        BooleanExpression repExpr = hasText(representativeName) ? QFactory.factory.representativeName.containsIgnoreCase(representativeName) : null;
        BooleanExpression bizExpr = hasText(businessNumber) ? QFactory.factory.businessNumber.containsIgnoreCase(businessNumber) : null;
        if (repExpr != null && bizExpr != null) return repExpr.or(bizExpr);
        if (repExpr != null) return repExpr;
        return bizExpr;
    }

    private BooleanExpression regionEq(Region region) {
        return region != null ? QFactory.factory.region.eq(region) : null;
    }

    private BooleanExpression statusEq(UsableStatus status) {
        return status != null ? QFactory.factory.status.eq(status) : null;
    }
}
