package com.chaing.api.dto.hq.user.request;

import com.chaing.domain.users.dto.condition.UserSearchCondition;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.enums.UserStatus;

import java.util.List;

public record UserSearchRequest(

        String loginId,
        String username,
        String employeeNumber,
        UserRole role,
        UserPosition position,
        UserStatus status,
        Long businessUnitId,
        String orgName
) {
    public UserSearchCondition toCondition(java.util.List<Long> hqIds, java.util.List<Long> franchiseIds, List<Long> factoryIds) {
        return new UserSearchCondition(
                loginId,
                username,
                employeeNumber,
                role,
                position,
                status,
                businessUnitId,
                hqIds,
                franchiseIds,
                factoryIds
        );
    }
}
