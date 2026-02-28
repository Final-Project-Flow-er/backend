package com.chaing.domain.users.dto.command;

public record MyInfoUpdateCommand(

        String email,
        String phone,
        String profileImageUrl
) {
}
