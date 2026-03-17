package com.chaing.api.dto.user.response;

import com.chaing.domain.users.enums.UserRole;
import lombok.Builder;

@Builder
public record LoginResponse(

        String accessToken,
        String refreshToken,
        UserRole userRole
) {
}
