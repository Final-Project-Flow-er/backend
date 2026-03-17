package com.chaing.domain.users.dto.condition;

import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.enums.UserStatus;

public record UserSearchCondition(

        String loginId,
        String username,
        String employeeNumber,
        UserRole role,
        UserPosition position,
        UserStatus status,
        Long businessUnitId
) {
}
