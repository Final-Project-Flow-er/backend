package com.chaing.api.dto.user.request;

import com.chaing.domain.users.dto.command.MyInfoUpdateCommand;

public record UpdateMyInfoRequest(

        String email,
        String phone,
        String profileImageUrl
) {
    public MyInfoUpdateCommand toCommand() {
        return new MyInfoUpdateCommand(email, phone, profileImageUrl);
    }
}
