package com.chaing.domain.users.repository.impl;

import com.chaing.domain.users.dto.condition.UserSearchCondition;
import com.chaing.domain.users.entity.QUser;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.enums.UserStatus;
import com.chaing.domain.users.repository.interfaces.UserRepositoryCustom;
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
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> searchUsers(UserSearchCondition condition, Pageable pageable) {

        QUser user = QUser.user;

        List<User> content = queryFactory
                .selectFrom(user)
                .where(
                        loginIdOrUsernameOrEmployeeNumber(condition.loginId(), condition.username(), condition.employeeNumber()),
                        roleEq(condition.role()),
                        positionEq(condition.position()),
                        statusEq(condition.status()),
                        businessUnitIdEq(condition.businessUnitId()),
                        orgNameIdsMatch(condition)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(user.userId.desc())
                .fetch();

        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        loginIdOrUsernameOrEmployeeNumber(condition.loginId(), condition.username(), condition.employeeNumber()),
                        roleEq(condition.role()),
                        positionEq(condition.position()),
                        statusEq(condition.status()),
                        businessUnitIdEq(condition.businessUnitId()),
                        orgNameIdsMatch(condition)
                )
                .fetchOne();

        long totalCount = (total != null) ? total : 0L;
        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression loginIdOrUsernameOrEmployeeNumber(String loginId, String username,
            String employeeNumber) {
        BooleanExpression loginIdExpr = hasText(loginId) ? QUser.user.loginId.containsIgnoreCase(loginId) : null;
        BooleanExpression usernameExpr = hasText(username) ? QUser.user.username.containsIgnoreCase(username) : null;
        BooleanExpression empNumExpr = hasText(employeeNumber) ? QUser.user.employeeNumber.containsIgnoreCase(employeeNumber) : null;

        BooleanExpression result = null;
        if (loginIdExpr != null) result = loginIdExpr;
        if (usernameExpr != null) result = result != null ? result.or(usernameExpr) : usernameExpr;
        if (empNumExpr != null) result = result != null ? result.or(empNumExpr) : empNumExpr;
        return result;
    }

    private BooleanExpression roleEq(UserRole role) {
        return role != null ? QUser.user.role.eq(role) : null;
    }

    private BooleanExpression positionEq(UserPosition position) {
        return position != null ? QUser.user.position.eq(position) : null;
    }

    private BooleanExpression statusEq(UserStatus status) {
        return status != null ? QUser.user.status.eq(status) : null;
    }

    private BooleanExpression businessUnitIdEq(Long businessUnitId) {
        return businessUnitId != null ? QUser.user.businessUnitId.eq(businessUnitId) : null;
    }

    private BooleanExpression orgNameIdsMatch(UserSearchCondition condition) {
        if (condition.hqIds() == null && condition.franchiseIds() == null && condition.factoryIds() == null) {
            return null;
        }

        BooleanExpression hqMatch = (condition.hqIds() != null && !condition.hqIds().isEmpty()) ? QUser.user.role.eq(UserRole.HQ).and(QUser.user.businessUnitId.in(condition.hqIds())) : null;
        BooleanExpression franchiseMatch = (condition.franchiseIds() != null && !condition.franchiseIds().isEmpty()) ? QUser.user.role.eq(UserRole.FRANCHISE).and(QUser.user.businessUnitId.in(condition.franchiseIds())) : null;
        BooleanExpression factoryMatch = (condition.factoryIds() != null && !condition.factoryIds().isEmpty()) ? QUser.user.role.eq(UserRole.FACTORY).and(QUser.user.businessUnitId.in(condition.factoryIds())) : null;

        BooleanExpression result = null;
        if (hqMatch != null) result = hqMatch;
        if (franchiseMatch != null) result = (result != null) ? result.or(franchiseMatch) : franchiseMatch;
        if (factoryMatch != null) result = (result != null) ? result.or(factoryMatch) : factoryMatch;

        return result != null ? result : QUser.user.userId.isNull();
    }
}
