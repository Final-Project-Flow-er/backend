package com.chaing.domain.users.event;

public record UserRegisteredEvent(
        String email,
        String loginId,
        String tempPassword,
        String employeeNumber
) {
}
