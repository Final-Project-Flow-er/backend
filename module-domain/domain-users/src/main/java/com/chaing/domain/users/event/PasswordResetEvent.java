package com.chaing.domain.users.event;

public record PasswordResetEvent(
        String email,
        String tempPassword
) {
}
