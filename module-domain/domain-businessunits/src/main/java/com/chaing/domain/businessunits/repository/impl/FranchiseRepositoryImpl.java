package com.chaing.domain.businessunits.repository.impl;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.condition.BusinessUnitSearchCondition;
import com.chaing.domain.businessunits.entity.Franchise;
import com.chaing.domain.businessunits.entity.QFranchise;
import com.chaing.domain.businessunits.repository.interfaces.FranchiseRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class FranchiseRepositoryImpl implements FranchiseRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Franchise> search(BusinessUnitSearchCondition condition, Pageable pageable) {

        QFranchise franchise = QFranchise.franchise;

        List<Franchise> content = queryFactory
                .selectFrom(franchise)
                .where(
                        nameOrCodeContains(condition.code(), condition.name()),
                        representativeOrBusinessNumberContains(condition.representativeName(), condition.businessNumber()),
                        regionEq(condition.region()),
                        statusEq(condition.status()),
                        operatingDaysContains(condition.operatingDays()),
                        returnBlockedEq(condition.isReturnBlocked())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(franchise.franchiseId.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(franchise.count())
                .from(franchise)
                .where(
                        nameOrCodeContains(condition.code(), condition.name()),
                        representativeOrBusinessNumberContains(condition.representativeName(), condition.businessNumber()),
                        regionEq(condition.region()),
                        statusEq(condition.status()),
                        operatingDaysContains(condition.operatingDays()),
                        returnBlockedEq(condition.isReturnBlocked())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameOrCodeContains(String code, String name) {
        BooleanExpression codeExpr = hasText(code) ? QFranchise.franchise.franchiseCode.containsIgnoreCase(code) : null;
        BooleanExpression nameExpr = hasText(name) ? QFranchise.franchise.name.containsIgnoreCase(name) : null;
        if (codeExpr != null && nameExpr != null) return codeExpr.or(nameExpr);
        if (codeExpr != null) return codeExpr;
        return nameExpr;
    }

    private BooleanExpression representativeOrBusinessNumberContains(String representativeName, String businessNumber) {
        BooleanExpression repExpr = hasText(representativeName) ? QFranchise.franchise.representativeName.containsIgnoreCase(representativeName) : null;
        BooleanExpression bizExpr = hasText(businessNumber) ? QFranchise.franchise.businessNumber.containsIgnoreCase(businessNumber) : null;
        if (repExpr != null && bizExpr != null) return repExpr.or(bizExpr);
        if (repExpr != null) return repExpr;
        return bizExpr;
    }

    private BooleanExpression regionEq(Region region) {
        return region != null ? QFranchise.franchise.region.eq(region) : null;
    }

    private BooleanExpression statusEq(UsableStatus status) {
        return status != null ? QFranchise.franchise.status.eq(status) : null;
    }

    private BooleanExpression operatingDaysContains(String operatingDays) {
        return hasText(operatingDays) ? QFranchise.franchise.operatingDays.contains(operatingDays) : null;
    }

    private BooleanExpression returnBlockedEq(Boolean isBlocked) {
        if (isBlocked == null) return null;

        LocalDateTime now = LocalDateTime.now();
        if (isBlocked) {
            return QFranchise.franchise.penaltyEndDate.after(now);
        } else {
            return QFranchise.franchise.penaltyEndDate.before(now)
                    .or(QFranchise.franchise.penaltyEndDate.isNull());
        }
    }
}
