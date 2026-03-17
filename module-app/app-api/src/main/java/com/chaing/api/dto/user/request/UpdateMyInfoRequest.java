package com.chaing.api.dto.user.request;

import com.chaing.domain.users.dto.command.MyInfoUpdateCommand;
import jakarta.validation.constraints.Email;

public record UpdateMyInfoRequest(

        @Email(message = "올바른 이메일 형식이 아닙니다.")
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
