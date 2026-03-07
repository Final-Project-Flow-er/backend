package com.chaing.domain.users.repository.impl;

import com.chaing.domain.users.dto.condition.UserLogSearchCondition;
import com.chaing.domain.users.entity.QUserLog;
import com.chaing.domain.users.entity.UserLog;
import com.chaing.domain.users.enums.UserAction;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.repository.interfaces.UserLogRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class UserLogRepositoryImpl implements UserLogRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UserLog> searchUserLogs(UserLogSearchCondition condition, Pageable pageable) {

        QUserLog userLog = QUserLog.userLog;

        List<UserLog> content = queryFactory
                .selectFrom(userLog)
                .where(
                        targetUserIdEq(condition.targetUserId()),
                        actorIdEq(condition.actorId()),
                        actionEq(condition.action()),
                        targetUsernameContains(condition.targetUsername()),
                        employeeNumberContains(condition.employeeNumber()),
                        roleEq(condition.role()),
                        positionEq(condition.position()),
                        createdAtEq(condition.createdAt())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(userLog.logId.desc())
                .fetch();

        Long total = queryFactory
                .select(userLog.count())
                .from(userLog)
                .where(
                        targetUserIdEq(condition.targetUserId()),
                        actorIdEq(condition.actorId()),
                        actionEq(condition.action()),
                        targetUsernameContains(condition.targetUsername()),
                        employeeNumberContains(condition.employeeNumber()),
                        roleEq(condition.role()),
                        positionEq(condition.position()),
                        createdAtEq(condition.createdAt())
                )
                .fetchOne();

        long totalCount = (total != null) ? total : 0L;
        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression targetUserIdEq(Long targetUserId) {
        return targetUserId != null ? QUserLog.userLog.targetUserId.eq(targetUserId) : null;
    }

    private BooleanExpression actorIdEq(Long actorId) {
        return actorId != null ? QUserLog.userLog.actorId.eq(actorId) : null;
    }

    private BooleanExpression actionEq(UserAction action) {
        return action != null ? QUserLog.userLog.action.eq(action) : null;
    }

    private BooleanExpression targetUsernameContains(String targetUsername) {
        return hasText(targetUsername) ? QUserLog.userLog.targetUsername.contains(targetUsername) : null;
    }

    private BooleanExpression employeeNumberContains(String employeeNumber) {
        return hasText(employeeNumber) ? QUserLog.userLog.employeeNumber.contains(employeeNumber) : null;
    }

    private BooleanExpression roleEq(UserRole role) {
        return role != null ? QUserLog.userLog.role.eq(role) : null;
    }

    private BooleanExpression positionEq(UserPosition position) {
        return position != null ? QUserLog.userLog.position.eq(position) : null;
    }

    private BooleanExpression createdAtEq(LocalDateTime createdAt) {
        if (createdAt == null) return null;
        LocalDateTime startOfDay = createdAt.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = createdAt.toLocalDate().atTime(23, 59, 59);
        return QUserLog.userLog.createdAt.between(startOfDay, endOfDay);
    }
}