package com.chaing.api.dto.user.event;

public record UserInfoResendEvent(
        String email,
        String loginId,
        String employeeNumber
) {
}
