package com.chaing.api.dto.hq.user.request;

import com.chaing.domain.users.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(

        @NotNull(message = "변경할 상태값은 필수입니다.")
        UserStatus status
) {
}
