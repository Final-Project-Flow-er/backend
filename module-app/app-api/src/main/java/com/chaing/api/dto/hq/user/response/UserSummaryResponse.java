package com.chaing.api.dto.hq.user.response;

import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.enums.UserStatus;
import lombok.Builder;

@Builder
public record UserSummaryResponse(

        String loginId,
        String username,
        String employeeNumber,
        String email,
        String phone,
        UserRole role,
        UserPosition position,
        UserStatus status
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getLoginId(),
                user.getUsername(),
                user.getEmployeeNumber(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getPosition(),
                user.getStatus()
        );
    }
}
