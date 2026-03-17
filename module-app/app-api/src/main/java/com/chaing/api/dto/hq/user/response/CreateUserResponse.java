package com.chaing.api.dto.hq.user.response;

import com.chaing.domain.users.entity.User;
import lombok.Builder;

@Builder
public record CreateUserResponse(

        Long userId,
        String loginId,
        String employeeNumber,
        String username,
        String email
) {
    public static CreateUserResponse from(User user) {
        return CreateUserResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .employeeNumber(user.getEmployeeNumber())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
