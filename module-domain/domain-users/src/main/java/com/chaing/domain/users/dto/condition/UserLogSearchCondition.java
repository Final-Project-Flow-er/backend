package com.chaing.domain.users.dto.condition;

import com.chaing.domain.users.enums.UserAction;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;

import java.time.LocalDateTime;

public record UserLogSearchCondition(

        Long targetUserId,
        Long actorId,
        UserAction action,
        String targetUsername,
        String employeeNumber,
        UserRole role,
        UserPosition position,
        LocalDateTime createdAt
) {
}
