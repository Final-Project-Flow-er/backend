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
                        codeContains(condition.code()),
                        nameContains(condition.name()),
                        representativeContains(condition.representativeName()),
                        businessNumberContains(condition.businessNumber()),
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
                        codeContains(condition.code()),
                        nameContains(condition.name()),
                        representativeContains(condition.representativeName()),
                        businessNumberContains(condition.businessNumber()),
                        regionEq(condition.region()),
                        statusEq(condition.status())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression codeContains(String code) {
        return hasText(code) ? QFactory.factory.factoryCode.contains(code) : null;
    }

    private BooleanExpression nameContains(String name) {
        return hasText(name) ? QFactory.factory.name.contains(name) : null;
    }

    private BooleanExpression representativeContains(String representativeName) {
        return hasText(representativeName) ? QFactory.factory.representativeName.contains(representativeName) : null;
    }

    private BooleanExpression businessNumberContains(String businessNumber) {
        return hasText(businessNumber) ? QFactory.factory.businessNumber.contains(businessNumber) : null;
    }

    private BooleanExpression regionEq(Region region) {
        return region != null ? QFactory.factory.region.eq(region) : null;
    }

    private BooleanExpression statusEq(UsableStatus status) {
        return status != null ? QFactory.factory.status.eq(status) : null;
    }
}
