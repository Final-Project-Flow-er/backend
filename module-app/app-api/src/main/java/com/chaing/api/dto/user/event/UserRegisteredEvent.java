package com.chaing.api.dto.user.event;

public record UserRegisteredEvent(
        String email,
        String loginId,
        String tempPassword,
        String employeeNumber
) {
}
