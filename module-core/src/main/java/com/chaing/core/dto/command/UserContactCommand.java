package com.chaing.core.dto.command;

public record UserContactCommand(
        String username,

        String phoneNumber
) {
}
