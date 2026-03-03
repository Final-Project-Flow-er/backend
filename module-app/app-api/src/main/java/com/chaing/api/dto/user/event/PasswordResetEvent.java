package com.chaing.api.dto.user.event;

public record PasswordResetEvent(
        String email,
        String tempPassword
) {
}
