package com.chaing.api.dto.user.request;

import com.chaing.domain.users.dto.command.MyInfoUpdateCommand;

public record UpdateMyInfoRequest(

        String email,
        String phone,
        String profileImageUrl
) {
    public MyInfoUpdateCommand toCommand(String newFileName) {
        return new MyInfoUpdateCommand(
                this.email,
                this.phone,
                (newFileName != null) ? newFileName : this.profileImageUrl
        );
    }
}
