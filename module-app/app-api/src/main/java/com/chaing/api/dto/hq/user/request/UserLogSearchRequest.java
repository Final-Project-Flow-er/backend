package com.chaing.api.dto.hq.user.request;

import com.chaing.domain.users.dto.condition.UserLogSearchCondition;
import com.chaing.domain.users.enums.UserAction;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;

import java.time.LocalDateTime;

public record UserLogSearchRequest(

        Long targetUserId,
        Long actorId,
        UserAction action,
        String targetUsername,
        String employeeNumber,
        UserRole role,
        UserPosition position,
        LocalDateTime createdAt
) {
    public UserLogSearchCondition toCondition() {
        return new UserLogSearchCondition(
                targetUserId,
                actorId,
                action,
                targetUsername,
                employeeNumber,
                role,
                position,
                createdAt
        );
    }
}
