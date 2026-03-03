package com.chaing.api.dto.user.response;

import lombok.Builder;

@Builder
public record LoginResponse(

        String accessToken,
        String refreshToken
) {
}
