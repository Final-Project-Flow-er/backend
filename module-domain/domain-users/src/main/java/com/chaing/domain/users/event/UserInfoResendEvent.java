package com.chaing.domain.users.event;

public record UserInfoResendEvent(
        String email,
        String loginId,
        String employeeNumber
) {
}
